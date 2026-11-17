const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot list, read, or write any user collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("saved_translations").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("collected_items").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("chat_messages").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("collected_items").doc("item_1").set({
      userId: ALICE_UID,
      title: "Unauth",
      category: "GENERAL",
      summary: "Should fail",
      extractedFacts: [],
      sourceType: "TEXT",
      sourcePrompt: "test",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Owner can create, list, update, and delete saved_translations; cross-user & ghost fields blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  const validTranslation = {
    userId: ALICE_UID,
    sourceLang: "English",
    targetLang: "Hindi (हिन्दी)",
    targetCode: "HI",
    targetScript: "Devanagari",
    sourceText: "Where can I find safe drinking water?",
    translatedText: "यहाँ मुझे पीने का साफ़ पानी कहाँ मिलेगा?",
    phoneticGuide: "Yahan mujhe peene ka saaf paani kahan milega?",
    createdAt: now,
    updatedAt: now,
  };

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_translations").doc("tr_1").set(validTranslation)
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_translations").get()
  );

  // Partial update succeeds without changing createdAt or userId
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_translations").doc("tr_1").update({
      phoneticGuide: "Updated phonetic guide",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject @DocumentId 'id' in payload
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_translations").doc("tr_2").set({
      id: "tr_2",
      ...validTranslation,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_translations").doc("tr_3").set({
      ...validTranslation,
      createdAt: new Date(Date.now() + 60000),
    })
  );

  // Cross-user isolation
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("saved_translations").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("saved_translations").doc("tr_1").delete()
  );

  // Owner delete succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_translations").doc("tr_1").delete()
  );
});

test("Owner can create, update, and query collected_items; invalid enum or ghost field rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  const validCollected = {
    userId: ALICE_UID,
    title: "Railway Station Sign",
    category: "SIGN_OCR",
    summary: "Platform 3 leads to New Delhi Express.",
    extractedFacts: ["Platform: 3", "Train: New Delhi Express", "Gate: East Exit"],
    sourceType: "PHOTO",
    sourcePrompt: "What does this station sign say?",
    createdAt: now,
    updatedAt: now,
  };

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("collected_items").doc("ci_1").set(validCollected)
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("collected_items").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("collected_items").doc("ci_1").update({
      title: "Updated Station Sign",
      category: "TRAVEL",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Shadow update with ghost field rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("collected_items").doc("ci_1").update({
      isVerified: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Invalid category enum rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("collected_items").doc("ci_2").set({
      ...validCollected,
      category: "INVALID_CAT",
    })
  );

  // Cross-user access rejected
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("collected_items").doc("ci_1").get()
  );
});

test("Owner can create and list chat_messages; updates forbidden and optional fields validated", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("chat_messages").doc("msg_1").set({
      userId: ALICE_UID,
      role: "USER",
      text: "Extract details from this menu photo",
      hasPhoto: true,
      photoCaption: "Attached photo",
      createdAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("chat_messages").doc("msg_2").set({
      userId: ALICE_UID,
      role: "ASSISTANT",
      text: "Here are the vegetarian dishes and prices.",
      hasPhoto: false,
      extractedTitle: "Menu Highlights",
      extractedCategory: "TRAVEL",
      extractedSummary: "South Indian breakfast items with prices in INR.",
      extractedFacts: ["Masala Dosa: ₹80", "Filter Coffee: ₹35"],
      createdAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("chat_messages").get()
  );

  // Updates to immutable chat messages rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("chat_messages").doc("msg_1").update({
      text: "Tampered",
    })
  );

  // Cross-user read rejected
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("chat_messages").get()
  );
});
