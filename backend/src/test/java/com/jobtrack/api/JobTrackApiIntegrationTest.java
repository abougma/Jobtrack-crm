package com.jobtrack.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JobTrackApiIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("jobtrack")
            .withUsername("jobtrack")
            .withPassword("jobtrack-test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("jobtrack.security.jwt-secret", () -> "integration-test-secret-at-least-32-characters");
        registry.add("jobtrack.admin.emails", () -> ADMIN_EMAIL);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final String ADMIN_EMAIL = "platform-admin@jobtrack.test";

    @Test
    void supportsTheFullJobSeekerJourney() {
        String email = "ada-" + UUID.randomUUID() + "@example.com";
        Map<String, Object> credentials = Map.of("email", email, "password", "motdepasse-solide-123");

        ResponseEntity<Map<String, Object>> register = restTemplate.exchange(
                "/api/v1/auth/register", HttpMethod.POST, json(credentials), MAP_TYPE);
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(register.getBody()).containsKeys("accessToken", "refreshToken", "user");
        String token = (String) register.getBody().get("accessToken");

        ResponseEntity<Map<String, Object>> me = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET, authorized(token), MAP_TYPE);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody()).containsEntry("email", email);
        assertThat(me.getBody()).containsEntry("plan", "FREE");

        Map<String, Object> applicationBody = new HashMap<>();
        applicationBody.put("company", "Acme");
        applicationBody.put("position", "Développeur Java");
        applicationBody.put("status", "APPLIED");
        applicationBody.put("source", "LinkedIn");
        ResponseEntity<Map<String, Object>> created = restTemplate.exchange(
                "/api/v1/applications", HttpMethod.POST, json(applicationBody, token), MAP_TYPE);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String applicationId = (String) created.getBody().get("id");
        assertThat(applicationId).isNotBlank();

        ResponseEntity<Map<String, Object>> list = restTemplate.exchange(
                "/api/v1/applications?size=20", HttpMethod.GET, authorized(token), MAP_TYPE);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) list.getBody().get("content")).hasSize(1);

        ResponseEntity<Map<String, Object>> patched = restTemplate.exchange(
                "/api/v1/applications/" + applicationId + "/status", HttpMethod.PATCH,
                json(Map.of("status", "INTERVIEW"), token), MAP_TYPE);
        assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patched.getBody()).containsEntry("status", "INTERVIEW");

        ResponseEntity<Map<String, Object>> dashboard = restTemplate.exchange(
                "/api/v1/dashboard", HttpMethod.GET, authorized(token), MAP_TYPE);
        assertThat(dashboard.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(dashboard.getBody()).containsEntry("totalApplications", 1);
        assertThat(dashboard.getBody()).containsEntry("interviews", 1);

        Map<String, Object> analyzeBody = Map.of(
                "offerText", "Nous recherchons un développeur Java avec Spring Boot, Docker et PostgreSQL.");
        ResponseEntity<Map<String, Object>> analysis = restTemplate.exchange(
                "/api/v1/cv/analyze", HttpMethod.POST, json(analyzeBody, token), MAP_TYPE);
        assertThat(analysis.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(analysis.getBody()).containsKeys("score", "coveredSkills", "missingSkills");

        ResponseEntity<Map<String, Object>> plan = restTemplate.exchange(
                "/api/v1/plan", HttpMethod.GET, authorized(token), MAP_TYPE);
        assertThat(plan.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(plan.getBody()).containsEntry("used", 1);
        assertThat(plan.getBody()).containsEntry("plan", "FREE");

        Map<String, Object> generateBody = new HashMap<>();
        generateBody.put("offerText", "Nous recherchons un développeur Java avec Spring Boot, Docker et PostgreSQL.");
        generateBody.put("applicationId", applicationId);
        generateBody.put("title", "CV — Acme");
        ResponseEntity<Map<String, Object>> generated = restTemplate.exchange(
                "/api/v1/cv/generate", HttpMethod.POST, json(generateBody, token), MAP_TYPE);
        assertThat(generated.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(generated.getBody()).containsKeys("id", "title", "score", "missingInformation");
        assertThat(generated.getBody()).containsEntry("title", "CV — Acme");
        String cvId = (String) generated.getBody().get("id");

        ResponseEntity<List<Map<String, Object>>> cvList = restTemplate.exchange(
                "/api/v1/cv", HttpMethod.GET, authorized(token),
                new ParameterizedTypeReference<>() {});
        assertThat(cvList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cvList.getBody()).hasSize(1);

        ResponseEntity<Map<String, Object>> cvDetail = restTemplate.exchange(
                "/api/v1/cv/" + cvId, HttpMethod.GET, authorized(token), MAP_TYPE);
        assertThat(cvDetail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cvDetail.getBody()).containsEntry("id", cvId);

        ResponseEntity<byte[]> cvExport = restTemplate.exchange(
                "/api/v1/cv/" + cvId + "/export?format=txt", HttpMethod.GET, authorized(token),
                byte[].class);
        assertThat(cvExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cvExport.getBody()).isNotEmpty();

        ResponseEntity<Map<String, Object>> organization = restTemplate.exchange(
                "/api/v1/organizations", HttpMethod.POST,
                json(Map.of("name", "École 42"), token), MAP_TYPE);
        assertThat(organization.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String organizationId = (String) organization.getBody().get("id");
        assertThat(organization.getBody()).containsEntry("role", "ORG_ADMIN");

        ResponseEntity<Map<String, Object>> consentBefore = restTemplate.exchange(
                "/api/v1/organizations/" + organizationId + "/consent", HttpMethod.GET,
                authorized(token), MAP_TYPE);
        assertThat(consentBefore.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(consentBefore.getBody()).containsEntry("active", false);

        ResponseEntity<Map<String, Object>> consentAfter = restTemplate.exchange(
                "/api/v1/organizations/" + organizationId + "/consent", HttpMethod.PUT,
                json(Map.of("scope", "AGGREGATED"), token), MAP_TYPE);
        assertThat(consentAfter.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(consentAfter.getBody()).containsEntry("active", true);

        ResponseEntity<Map<String, Object>> organizationDashboard = restTemplate.exchange(
                "/api/v1/organizations/" + organizationId + "/dashboard", HttpMethod.GET,
                authorized(token), MAP_TYPE);
        assertThat(organizationDashboard.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(organizationDashboard.getBody()).containsEntry("members", 1);

        ResponseEntity<Map<String, Object>> exported = restTemplate.exchange(
                "/api/v1/privacy/export", HttpMethod.GET, authorized(token), MAP_TYPE);
        assertThat(exported.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exported.getBody()).containsKey("applications");
        assertThat((List<?>) exported.getBody().get("applications")).hasSize(1);

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/api/v1/privacy/account", HttpMethod.DELETE,
                json(Map.of("confirm", true), token), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map<String, Object>> afterDeletion = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET, authorized(token), MAP_TYPE);
        assertThat(afterDeletion.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void importsAndExportsCsvApplications() {
        String email = "csv-" + UUID.randomUUID() + "@example.com";
        ResponseEntity<Map<String, Object>> register = restTemplate.exchange(
                "/api/v1/auth/register", HttpMethod.POST,
                json(Map.of("email", email, "password", "motdepasse-solide-123")), MAP_TYPE);
        String token = (String) register.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", new ByteArrayResource(
                "company;position;status;notes\nBeta;DevOps;APPLIED;'=SUM(A1:A2)\n".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "candidatures.csv";
            }
        });

        ResponseEntity<Map<String, Object>> imported = restTemplate.exchange(
                "/api/v1/applications/import", HttpMethod.POST, new HttpEntity<>(form, headers), MAP_TYPE);
        assertThat(imported.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(imported.getBody()).containsEntry("imported", 1);
        assertThat(imported.getBody()).containsEntry("skipped", 0);

        ResponseEntity<byte[]> exported = restTemplate.exchange(
                "/api/v1/applications/export.csv", HttpMethod.GET, authorized(token), byte[].class);
        assertThat(exported.getStatusCode()).isEqualTo(HttpStatus.OK);
        String csv = new String(exported.getBody(), StandardCharsets.UTF_8);
        assertThat(csv).contains("company;position;");
        assertThat(csv).contains("Beta;DevOps");
        assertThat(csv).contains("'=SUM(A1:A2)");
        assertThat(csv).doesNotContain(";=SUM(A1:A2)");
    }

    @Test
    void platformAdminManagesUsersPlansAndOrganizations() {
        String memberEmail = "member-" + UUID.randomUUID() + "@example.com";
        ResponseEntity<Map<String, Object>> member = restTemplate.exchange(
                "/api/v1/auth/register", HttpMethod.POST,
                json(Map.of("email", memberEmail, "password", "motdepasse-solide-123")), MAP_TYPE);
        String memberToken = (String) member.getBody().get("accessToken");
        Map<?, ?> memberUser = (Map<?, ?>) member.getBody().get("user");
        String memberId = (String) memberUser.get("id");
        assertThat(memberUser.get("role")).isEqualTo("JOB_SEEKER");

        ResponseEntity<Map<String, Object>> forbidden = restTemplate.exchange(
                "/api/v1/admin/overview", HttpMethod.GET, authorized(memberToken), MAP_TYPE);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Map<String, Object>> admin = restTemplate.exchange(
                "/api/v1/auth/register", HttpMethod.POST,
                json(Map.of("email", ADMIN_EMAIL, "password", "motdepasse-solide-123")), MAP_TYPE);
        assertThat(admin.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<?, ?> adminUser = (Map<?, ?>) admin.getBody().get("user");
        String adminId = (String) adminUser.get("id");
        String adminToken = (String) admin.getBody().get("accessToken");
        assertThat(adminUser.get("role")).isEqualTo("PLATFORM_ADMIN");

        ResponseEntity<Map<String, Object>> overview = restTemplate.exchange(
                "/api/v1/admin/overview", HttpMethod.GET, authorized(adminToken), MAP_TYPE);
        assertThat(overview.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(overview.getBody()).containsKeys("totalUsers", "platformAdmins", "totalOrganizations",
                "totalMemberships", "totalApplications", "usersByPlan");
        assertThat(((Number) overview.getBody().get("platformAdmins")).longValue()).isGreaterThanOrEqualTo(1);

        ResponseEntity<Map<String, Object>> users = restTemplate.exchange(
                "/api/v1/admin/users?query=" + memberEmail, HttpMethod.GET, authorized(adminToken), MAP_TYPE);
        assertThat(users.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> content = (List<?>) users.getBody().get("content");
        assertThat(content).hasSize(1);
        Map<?, ?> listed = (Map<?, ?>) content.get(0);
        assertThat(listed.get("email")).isEqualTo(memberEmail);
        assertThat(listed.get("plan")).isEqualTo("FREE");

        ResponseEntity<Map<String, Object>> detail = restTemplate.exchange(
                "/api/v1/admin/users/" + memberId, HttpMethod.GET, authorized(adminToken), MAP_TYPE);
        assertThat(detail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detail.getBody()).containsEntry("plan", "FREE");
        assertThat(detail.getBody()).containsEntry("applicationCount", 0);

        ResponseEntity<Map<String, Object>> updatedPlan = restTemplate.exchange(
                "/api/v1/admin/users/" + memberId + "/plan", HttpMethod.PATCH,
                json(Map.of("plan", "PRO"), adminToken), MAP_TYPE);
        assertThat(updatedPlan.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedPlan.getBody()).containsEntry("plan", "PRO");

        ResponseEntity<Map<String, Object>> memberMe = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET, authorized(memberToken), MAP_TYPE);
        assertThat(memberMe.getBody()).containsEntry("plan", "PRO");

        ResponseEntity<Map<String, Object>> selfDemotion = restTemplate.exchange(
                "/api/v1/admin/users/" + adminId + "/role", HttpMethod.PATCH,
                json(Map.of("role", "JOB_SEEKER"), adminToken), MAP_TYPE);
        assertThat(selfDemotion.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<Map<String, Object>> organizations = restTemplate.exchange(
                "/api/v1/admin/organizations", HttpMethod.GET, authorized(adminToken), MAP_TYPE);
        assertThat(organizations.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(organizations.getBody()).containsKey("content");

        String createdAdminEmail = "created-admin-" + UUID.randomUUID() + "@example.com";
        ResponseEntity<Map<String, Object>> createdAdmin = restTemplate.exchange(
                "/api/v1/admin/users", HttpMethod.POST,
                json(Map.of("email", createdAdminEmail, "password", "motdepasse-solide-123",
                        "role", "PLATFORM_ADMIN"), adminToken), MAP_TYPE);
        assertThat(createdAdmin.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdAdmin.getBody()).containsEntry("email", createdAdminEmail);
        assertThat(createdAdmin.getBody()).containsEntry("role", "PLATFORM_ADMIN");

        ResponseEntity<Map<String, Object>> duplicate = restTemplate.exchange(
                "/api/v1/admin/users", HttpMethod.POST,
                json(Map.of("email", createdAdminEmail, "password", "motdepasse-solide-123",
                        "role", "JOB_SEEKER"), adminToken), MAP_TYPE);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<Map<String, Object>> createdLogin = restTemplate.exchange(
                "/api/v1/auth/login", HttpMethod.POST,
                json(Map.of("email", createdAdminEmail, "password", "motdepasse-solide-123")), MAP_TYPE);
        assertThat(createdLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Map<?, ?>) createdLogin.getBody().get("user")).get("role")).isEqualTo("PLATFORM_ADMIN");
        String createdToken = (String) createdLogin.getBody().get("accessToken");
        ResponseEntity<Map<String, Object>> createdOverview = restTemplate.exchange(
                "/api/v1/admin/overview", HttpMethod.GET, authorized(createdToken), MAP_TYPE);
        assertThat(createdOverview.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map<String, Object>> weakPassword = restTemplate.exchange(
                "/api/v1/admin/users", HttpMethod.POST,
                json(Map.of("email", "weak-" + UUID.randomUUID() + "@example.com",
                        "password", "court", "role", "JOB_SEEKER"), adminToken), MAP_TYPE);
        assertThat(weakPassword.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsUnauthenticatedRequestsWithProblemDetails() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/applications", HttpMethod.GET, HttpEntity.EMPTY, MAP_TYPE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getContentType())
                .isNotNull()
                .matches(contentType -> contentType.getSubtype().equals("problem+json"));
        assertThat(response.getBody()).containsEntry("status", 401);
    }

    private HttpEntity<Object> json(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Object> json(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> authorized(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
}
