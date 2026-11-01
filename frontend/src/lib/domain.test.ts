import { describe, expect, it } from "vitest";
import {
  APPLICATION_STATUSES,
  STATUS_BADGE_VARIANTS,
  STATUS_LABEL_KEYS,
  WORK_MODES,
  WORK_MODE_LABEL_KEYS,
} from "./domain";
import { fr } from "./translations/fr";

describe("domain metadata", () => {
  it("provides a label key for every application status", () => {
    APPLICATION_STATUSES.forEach((status) => {
      expect(STATUS_LABEL_KEYS[status]).toBeDefined();
      expect(fr[STATUS_LABEL_KEYS[status]]).toBeTruthy();
      expect(STATUS_BADGE_VARIANTS[status]).toBeDefined();
    });
  });

  it("provides a label key for every work mode", () => {
    WORK_MODES.forEach((mode) => {
      expect(fr[WORK_MODE_LABEL_KEYS[mode]]).toBeTruthy();
    });
  });
});
