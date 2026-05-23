package com.disaster.alert.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.*;

public class ChatbotController implements Initializable {

    @FXML private VBox       messagesBox;
    @FXML private ScrollPane chatScroll;
    @FXML private TextField  chatInput;
    @FXML private Button     sendButton;
    @FXML private Label      statusLabel;

    // ── Knowledge Base ─────────────────────────────────────────────────────
    private static final Map<String[], String> KNOWLEDGE = new LinkedHashMap<>();

    static {
        KNOWLEDGE.put(new String[]{"earthquake", "quake", "tremor", "seismic"},
                "🌍 Earthquake Safety:\n\n" +
                        "DURING an earthquake:\n" +
                        "• DROP to hands and knees\n" +
                        "• Take COVER under a sturdy desk/table\n" +
                        "• HOLD ON until shaking stops\n" +
                        "• Stay away from windows & heavy objects\n" +
                        "• If outdoors, move away from buildings\n\n" +
                        "AFTER an earthquake:\n" +
                        "• Check for injuries & hazards\n" +
                        "• Expect aftershocks\n" +
                        "• Do NOT use elevators\n" +
                        "• Call 1122 (Pakistan Rescue) if needed");

        KNOWLEDGE.put(new String[]{"flood", "flooding", "flash flood", "water"},
                "🌊 Flood Safety:\n\n" +
                        "BEFORE a flood:\n" +
                        "• Move to higher ground immediately\n" +
                        "• Prepare emergency kit\n" +
                        "• Disconnect electrical appliances\n\n" +
                        "DURING a flood:\n" +
                        "• Never walk/drive through floodwater\n" +
                        "• 15cm of water can knock you down\n" +
                        "• 60cm of water can carry away a car\n\n" +
                        "AFTER a flood:\n" +
                        "• Avoid floodwater (may be contaminated)\n" +
                        "• Document damage for insurance\n" +
                        "• Call 1122 for rescue assistance");

        KNOWLEDGE.put(new String[]{"fire", "blaze", "burn", "smoke", "flame"},
                "🔥 Fire Safety:\n\n" +
                        "If fire breaks out:\n" +
                        "• Alert everyone — shout FIRE!\n" +
                        "• Call 16 (Fire Brigade Pakistan)\n" +
                        "• Activate nearest fire alarm\n" +
                        "• Evacuate using stairs, NOT elevator\n" +
                        "• Stay LOW — smoke rises\n" +
                        "• Feel doors before opening (back of hand)\n" +
                        "• If door is hot — don't open it\n" +
                        "• Meet at designated assembly point\n\n" +
                        "Prevention:\n" +
                        "• Install smoke detectors\n" +
                        "• Keep fire extinguisher accessible\n" +
                        "• Never leave cooking unattended");

        KNOWLEDGE.put(new String[]{"kit", "emergency kit", "bag", "supplies", "prepare", "preparation"},
                "🎒 Emergency Kit Checklist:\n\n" +
                        "Essential items (72-hour supply):\n" +
                        "• Water — 4 litres per person per day\n" +
                        "• Non-perishable food & can opener\n" +
                        "• First aid kit & medications\n" +
                        "• Flashlight & extra batteries\n" +
                        "• Battery-powered/hand-crank radio\n" +
                        "• Whistle (to signal for help)\n" +
                        "• Dust masks & plastic sheeting\n" +
                        "• Moist towelettes & garbage bags\n" +
                        "• Wrench/pliers to turn off utilities\n" +
                        "• Local maps (offline)\n" +
                        "• Important documents (copies)\n" +
                        "• Mobile phone with chargers & backup battery\n" +
                        "• Cash in small bills\n" +
                        "• Blankets & warm clothing");

        KNOWLEDGE.put(new String[]{"landslide", "mudslide", "rockfall", "avalanche"},
                "⛰️ Landslide Safety:\n\n" +
                        "Warning signs:\n" +
                        "• Unusual sounds (cracking trees, boulders)\n" +
                        "• Sudden increase/decrease in water flow\n" +
                        "• Tilting trees, fences, or walls\n\n" +
                        "DURING a landslide:\n" +
                        "• Move away from path QUICKLY\n" +
                        "• Run to higher ground\n" +
                        "• If escape impossible — curl into ball\n" +
                        "• Protect your head\n\n" +
                        "High-risk areas in Pakistan:\n" +
                        "• Gilgit-Baltistan, KPK, AJK\n" +
                        "• Especially during monsoon (July–Sept)");

        KNOWLEDGE.put(new String[]{"storm", "cyclone", "hurricane", "tornado", "wind", "thunder", "lightning"},
                "⛈️ Storm Safety:\n\n" +
                        "BEFORE a storm:\n" +
                        "• Secure outdoor furniture\n" +
                        "• Charge all devices\n" +
                        "• Fill bathtubs with water (backup supply)\n\n" +
                        "DURING a storm:\n" +
                        "• Stay indoors away from windows\n" +
                        "• Unplug electronics\n" +
                        "• If lightning: avoid tall trees & open fields\n" +
                        "• Do NOT use landline phones\n\n" +
                        "AFTER a storm:\n" +
                        "• Beware of downed power lines\n" +
                        "• Report outages to WAPDA: 118");

        KNOWLEDGE.put(new String[]{"first aid", "injury", "wound", "bleeding", "burn treatment", "cpr"},
                "🩺 First Aid Basics:\n\n" +
                        "Bleeding:\n" +
                        "• Apply firm pressure with clean cloth\n" +
                        "• Elevate the injured area\n" +
                        "• Do NOT remove embedded objects\n\n" +
                        "Burns:\n" +
                        "• Cool with running water (20 min)\n" +
                        "• Do NOT use ice, butter, or toothpaste\n" +
                        "• Cover with clean dressing\n\n" +
                        "CPR (if unresponsive):\n" +
                        "• 30 chest compressions (hard & fast)\n" +
                        "• 2 rescue breaths\n" +
                        "• Repeat until help arrives\n\n" +
                        "Emergency numbers Pakistan:\n" +
                        "• Rescue: 1122\n" +
                        "• Edhi Foundation: 115\n" +
                        "• Ambulance: 1122");

        KNOWLEDGE.put(new String[]{"heat", "heatwave", "hot", "dehydration", "sunstroke"},
                "☀️ Heatwave Safety:\n\n" +
                        "• Drink water every 30 minutes\n" +
                        "• Avoid outdoor activity 11am–3pm\n" +
                        "• Wear loose, light-coloured clothing\n" +
                        "• Use fans & stay in shade\n" +
                        "• Check on elderly neighbours\n\n" +
                        "Heat stroke signs:\n" +
                        "• High body temp (40°C+)\n" +
                        "• Confusion, no sweating\n" +
                        "• Call 1122 immediately\n" +
                        "• Cool person with wet cloths while waiting");

        KNOWLEDGE.put(new String[]{"pakistan", "emergency number", "helpline", "contact", "call", "rescue"},
                "📞 Pakistan Emergency Numbers:\n\n" +
                        "• 1122 — Rescue (Punjab)\n" +
                        "• 115  — Edhi Foundation (National)\n" +
                        "• 16   — Fire Brigade\n" +
                        "• 115  — Ambulance\n" +
                        "• 1033 — Police Emergency\n" +
                        "• 051-9205131 — NDMA (National)\n" +
                        "• 111-157-157 — PDMA Punjab\n\n" +
                        "NDMA = National Disaster Management Authority\n" +
                        "PDMA = Provincial Disaster Management Authority");

        KNOWLEDGE.put(new String[]{"alert", "severity", "level", "warning", "red", "orange", "yellow"},
                "🚨 Alert Severity Levels:\n\n" +
                        "🔴 RED (Critical):\n" +
                        "• Immediate threat to life\n" +
                        "• Evacuate NOW\n" +
                        "• Call emergency services\n\n" +
                        "🟠 ORANGE (High):\n" +
                        "• Significant hazard likely\n" +
                        "• Prepare to evacuate\n" +
                        "• Monitor updates closely\n\n" +
                        "🟡 YELLOW (Medium):\n" +
                        "• Potential hazard developing\n" +
                        "• Stay alert & prepare kit\n\n" +
                        "🟢 GREEN (Low):\n" +
                        "• Situation being monitored\n" +
                        "• Normal precautions apply");

        KNOWLEDGE.put(new String[]{"evacuation", "evacuate", "escape", "route", "shelter"},
                "🚗 Evacuation Guide:\n\n" +
                        "Planning:\n" +
                        "• Know 2 exit routes from your home\n" +
                        "• Designate a family meeting point\n" +
                        "• Keep fuel tank at least half full\n" +
                        "• Have 'go bag' ready (emergency kit)\n\n" +
                        "During evacuation:\n" +
                        "• Follow official routes only\n" +
                        "• Do NOT take shortcuts\n" +
                        "• Help elderly & disabled neighbours\n" +
                        "• Turn off gas, electricity, water\n" +
                        "• Lock your home & take documents");

        KNOWLEDGE.put(new String[]{"hello", "hi", "hey", "help", "what can you do"},
                "👋 Hello! I'm NADAS Assistant.\n\n" +
                        "I can help you with:\n" +
                        "• 🌍 Earthquake safety\n" +
                        "• 🌊 Flood preparedness\n" +
                        "• 🔥 Fire safety\n" +
                        "• ⛈️ Storm guidance\n" +
                        "• 🎒 Emergency kit checklist\n" +
                        "• 🩺 First aid basics\n" +
                        "• 📞 Pakistan emergency numbers\n" +
                        "• 🚨 Alert severity levels\n" +
                        "• 🚗 Evacuation planning\n\n" +
                        "Just type your question or tap a quick button above!");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addBotMessageDirect(
                "👋 Hello! I'm NADAS Assistant.\n\n" +
                        "I can help you with:\n" +
                        "• Disaster safety measures\n" +
                        "• Emergency procedures\n" +
                        "• Earthquake, flood & fire guidance\n" +
                        "• Pakistan emergency numbers\n\n" +
                        "Ask me anything about disaster safety!"
        );

        chatInput.setOnAction(e -> handleSend());

        messagesBox.heightProperty().addListener((obs, o, n) ->
                chatScroll.setVvalue(1.0));
    }

    @FXML
    private void handleSend() {
        String text = chatInput.getText().trim();
        if (text.isEmpty()) return;
        chatInput.clear();
        addUserMessage(text);
        setStatus("thinking");

        // Simulate slight delay for UX
        new Thread(() -> {
            try { Thread.sleep(600); } catch (InterruptedException ignored) {}
            String response = getLocalResponse(text);
            Platform.runLater(() -> {
                removeThinkingBubble();
                addBotMessageDirect(response);
                setStatus("online");
            });
        }).start();
    }

    @FXML private void handleChipEarthquake() {
        chatInput.setText("What should I do during an earthquake?");
        handleSend();
    }
    @FXML private void handleChipFlood() {
        chatInput.setText("How do I prepare for a flood?");
        handleSend();
    }
    @FXML private void handleChipFire() {
        chatInput.setText("What are fire safety tips?");
        handleSend();
    }
    @FXML private void handleChipKit() {
        chatInput.setText("What should be in an emergency kit?");
        handleSend();
    }

    // ── Local Knowledge Base Matching ──────────────────────────────────────

    private String getLocalResponse(String input) {
        String lower = input.toLowerCase().trim();

        // Find best match from knowledge base
        String bestMatch = null;
        int    bestScore = 0;

        for (Map.Entry<String[], String> entry : KNOWLEDGE.entrySet()) {
            int score = 0;
            for (String keyword : entry.getKey()) {
                if (lower.contains(keyword.toLowerCase())) {
                    // Longer keyword = more specific = higher score
                    score += keyword.length();
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestMatch = entry.getValue();
            }
        }

        if (bestMatch != null && bestScore > 0) {
            return bestMatch;
        }

        // Fallback response
        return "❓ I didn't quite understand that.\n\n" +
                "I can help you with:\n" +
                "• Earthquake / Flood / Fire safety\n" +
                "• Storm & landslide guidance\n" +
                "• Emergency kit preparation\n" +
                "• First aid basics\n" +
                "• Pakistan emergency numbers\n" +
                "• Alert severity levels\n\n" +
                "Try asking: 'What do I do in an earthquake?' or\n" +
                "tap one of the quick buttons above! 👆";
    }

    // ── UI Helpers ──────────────────────────────────────────────────────────

    private void addUserMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(240);
        label.getStyleClass().add("msg-user");

        HBox row = new HBox(label);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(4, 8, 4, 40));

        Platform.runLater(() -> messagesBox.getChildren().add(row));
    }

    private void addBotMessageDirect(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(260);
        label.getStyleClass().add("msg-bot");

        HBox row = new HBox(label);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 40, 4, 8));

        messagesBox.getChildren().add(row);
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    private void showThinkingBubble() {
        Label thinking = new Label("● Thinking...");
        thinking.getStyleClass().add("msg-thinking");
        HBox row = new HBox(thinking);
        row.setId("thinking-bubble");
        row.setPadding(new Insets(4, 40, 4, 8));
        Platform.runLater(() -> messagesBox.getChildren().add(row));
    }

    private void removeThinkingBubble() {
        messagesBox.getChildren().removeIf(
                n -> "thinking-bubble".equals(n.getId()));
    }

    private void setStatus(String state) {
        if ("thinking".equals(state)) {
            statusLabel.setText("● Thinking...");
            statusLabel.getStyleClass().setAll("chat-status-thinking");
            sendButton.setDisable(true);
            showThinkingBubble();
        } else {
            statusLabel.setText("● Online");
            statusLabel.getStyleClass().setAll("chat-status-online");
            sendButton.setDisable(false);
        }
    }
}