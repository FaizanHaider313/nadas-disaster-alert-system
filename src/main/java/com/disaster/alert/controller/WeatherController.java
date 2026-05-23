package com.disaster.alert.controller;

import com.disaster.alert.util.SceneManager;
import com.disaster.alert.util.SessionManager;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class WeatherController implements Initializable {

    private static final String API_KEY = "f708268fca7a4f63a97163511262004";
    private static final Random RNG = new Random();

    @FXML private Button    backButton;
    @FXML private TextField searchField;
    @FXML private Canvas    weatherCanvas;
    @FXML private StackPane rootPane;
    @FXML private StackPane heroPane;
    @FXML private Label     weatherIconLabel;
    @FXML private Label     cityNameLabel;
    @FXML private Label     countryLabel;
    @FXML private Label     tempLabel;
    @FXML private Label     feelsLikeLabel;
    @FXML private Label     conditionLabel;
    @FXML private Label     tipLabel;
    @FXML private Label     windLabel;
    @FXML private Label     humidityLabel;
    @FXML private Label     uvLabel;
    @FXML private Label     uvDescLabel;
    @FXML private Label     dewPointLabel;
    @FXML private Label     visibilityLabel;
    @FXML private Label     pressureLabel;
    @FXML private HBox      hourlyBox;
    @FXML private HBox      forecastBox;
    @FXML private Label     aqiLabel;
    @FXML private Label     aqiStatusLabel;
    @FXML private Label     pm25Label, pm10Label, o3Label, no2Label, so2Label, coLabel;
    @FXML private ProgressBar pm25Bar, pm10Bar, o3Bar, no2Bar;
    @FXML private Label     statusLabel;

    private AnimationTimer animTimer;
    private String         weatherType = "sunny";
    private final List<double[]> rainDrops  = new ArrayList<>();
    private final List<double[]> snowFlakes = new ArrayList<>();
    private final List<double[]> heatWaves  = new ArrayList<>();
    private final List<double[]> windLines  = new ArrayList<>();
    private final List<double[]> fogPuffs   = new ArrayList<>();
    private final List<double[]> clouds     = new ArrayList<>();
    private double lightningTimer = 0, lightningFlash = 0;
    private double canvasW = 0, canvasH = 0;
    private long   frameCount = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String city = "Islamabad";
        if (SessionManager.getInstance().isLoggedIn()) {
            String c = SessionManager.getInstance().getCurrentUser().getCity();
            if (c != null && !c.isEmpty()) city = c;
        }
        searchField.setText(city);

        weatherCanvas.widthProperty().bind(rootPane.widthProperty());
        weatherCanvas.heightProperty().bind(rootPane.heightProperty());
        weatherCanvas.widthProperty().addListener(e  -> { canvasW = weatherCanvas.getWidth();  resetParticles(); });
        weatherCanvas.heightProperty().addListener(e -> { canvasH = weatherCanvas.getHeight(); resetParticles(); });

        startAnimLoop();
        loadWeather(city);
    }

    private void startAnimLoop() {
        animTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                frameCount++;
                if (canvasW > 0 && canvasH > 0) drawFrame();
            }
        };
        animTimer.start();
    }

    private void drawFrame() {
        GraphicsContext gc = weatherCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasW, canvasH);
        switch (weatherType) {
            case "rain"   -> drawRain(gc, false);
            case "storm"  -> { drawRain(gc, true); drawLightning(gc); }
            case "snow"   -> drawSnow(gc);
            case "hot"    -> drawHeat(gc);
            case "windy"  -> drawWind(gc);
            case "fog"    -> drawFog(gc);
            case "cloudy" -> drawClouds(gc);
            case "sunny"  -> drawSun(gc);
        }
    }

    private void drawRain(GraphicsContext gc, boolean heavy) {
        int need = heavy ? 350 : 200;
        while (rainDrops.size() < need)
            rainDrops.add(new double[]{RNG.nextDouble()*canvasW, RNG.nextDouble()*canvasH,
                    8+RNG.nextDouble()*(heavy?14:8), heavy?3+RNG.nextDouble()*4:1+RNG.nextDouble(),
                    10+RNG.nextDouble()*(heavy?20:10), 0.3+RNG.nextDouble()*0.4});
        for (double[] d : rainDrops) {
            gc.setStroke(Color.rgb(174,214,241,d[5])); gc.setLineWidth(1);
            gc.strokeLine(d[0],d[1],d[0]+d[3],d[1]+d[4]);
            d[0]+=d[3]*0.6; d[1]+=d[2];
            if (d[1]>canvasH){d[1]=-d[4];d[0]=RNG.nextDouble()*canvasW;}
        }
    }

    private void drawLightning(GraphicsContext gc) {
        lightningTimer--;
        if (lightningFlash>0){
            gc.setFill(Color.rgb(255,255,255,lightningFlash*0.15));
            gc.fillRect(0,0,canvasW,canvasH);
            lightningFlash-=0.08;
        }
        if (lightningTimer<=0){
            lightningTimer=80+RNG.nextDouble()*200; lightningFlash=1.0;
            bolt(gc,canvasW*0.2+RNG.nextDouble()*canvasW*0.6,0,
                    canvasW*0.1+RNG.nextDouble()*canvasW*0.8,canvasH*0.55,0);
        }
    }

    private void bolt(GraphicsContext gc, double x1,double y1,double x2,double y2,int d) {
        if (d>4) return;
        double mx=(x1+x2)/2+(RNG.nextDouble()-0.5)*Math.abs(x2-x1)*0.9;
        double my=(y1+y2)/2+(RNG.nextDouble()-0.5)*Math.abs(y2-y1)*0.3;
        gc.setStroke(Color.rgb(200,220,255,d==0?0.9:0.5));
        gc.setLineWidth(Math.max(1,3-d));
        gc.strokeLine(x1,y1,mx,my); gc.strokeLine(mx,my,x2,y2);
        if (RNG.nextDouble()>0.5) bolt(gc,mx,my,mx+(RNG.nextDouble()-0.5)*140,my+90,d+1);
    }

    private void drawSnow(GraphicsContext gc) {
        while (snowFlakes.size()<160)
            snowFlakes.add(new double[]{RNG.nextDouble()*canvasW,RNG.nextDouble()*canvasH,
                    2+RNG.nextDouble()*5,0.4+RNG.nextDouble()*1.2,(RNG.nextDouble()-0.5)*0.8,
                    0.5+RNG.nextDouble()*0.5,RNG.nextDouble()*Math.PI*2});
        for (double[] s : snowFlakes) {
            s[6]+=0.02;
            gc.setFill(Color.rgb(220,240,255,s[5]));
            gc.fillOval(s[0]+Math.sin(s[6])*3-s[2],s[1]-s[2],s[2]*2,s[2]*2);
            s[1]+=s[3]; s[0]+=s[4];
            if (s[1]>canvasH){s[1]=-10;s[0]=RNG.nextDouble()*canvasW;}
            if (s[0]<0) s[0]=canvasW; if (s[0]>canvasW) s[0]=0;
        }
    }

    private void drawWind(GraphicsContext gc) {
        while (windLines.size()<60)
            windLines.add(new double[]{RNG.nextDouble()*canvasW,RNG.nextDouble()*canvasH,
                    4+RNG.nextDouble()*8,40+RNG.nextDouble()*100,0.1+RNG.nextDouble()*0.25,0.5+RNG.nextDouble()*1.5});
        for (double[] w : windLines) {
            gc.setStroke(Color.rgb(200,240,200,w[4])); gc.setLineWidth(w[5]);
            gc.strokeLine(w[0],w[1],w[0]+w[3],w[1]+(RNG.nextDouble()-0.5)*6);
            w[0]+=w[2];
            if (w[0]>canvasW+w[3]){w[0]=-w[3];w[1]=RNG.nextDouble()*canvasH;}
        }
    }
    private void drawHeat(GraphicsContext gc) {
        while (heatWaves.size() < 30)
            heatWaves.add(new double[]{RNG.nextDouble()*canvasW,
                    canvasH*0.55+RNG.nextDouble()*canvasH*0.35,
                    8+RNG.nextDouble()*22, 0.01+RNG.nextDouble()*0.02,
                    0.005+RNG.nextDouble()*0.01, RNG.nextDouble()*Math.PI*2,
                    0.03+RNG.nextDouble()*0.07});
        for (double[] h : heatWaves) {
            h[5] += h[4];
            double yOff = Math.sin(h[5]) * h[2];
            gc.setStroke(Color.rgb(255, 160, 40, h[6]));
            gc.setLineWidth(2);
            gc.beginPath();
            gc.moveTo(h[0], h[1]);
            gc.bezierCurveTo(h[0]+20, h[1]+yOff-18, h[0]+40, h[1]-yOff+18, h[0]+65, h[1]+yOff*0.5);
            gc.stroke();
        }
        // Smooth pulse instead of every-other-frame skip
        double pulse = 0.4 + 0.1 * Math.sin(frameCount * 0.04);
        double cx = canvasW * 0.82, cy = canvasH * 0.12;
        RadialGradient g = new RadialGradient(0, 0, cx, cy, canvasW * 0.28, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 120, 0, pulse)),
                new Stop(0.5, Color.rgb(255, 80, 0, pulse * 0.45)),
                new Stop(1, Color.TRANSPARENT));
        gc.setFill(g);
        gc.fillOval(cx - canvasW*0.28, cy - canvasW*0.28, canvasW*0.56, canvasW*0.56);
        gc.setFill(Color.rgb(255, 140, 0, 0.9));
        gc.fillOval(cx - 55, cy - 55, 110, 110);
    }

    private void drawFog(GraphicsContext gc) {
        while (fogPuffs.size()<8)
            fogPuffs.add(new double[]{RNG.nextDouble()*canvasW,
                    canvasH*0.25+RNG.nextDouble()*canvasH*0.55,
                    300+RNG.nextDouble()*400,150+RNG.nextDouble()*200,
                    0.05+RNG.nextDouble()*0.08,RNG.nextDouble()*Math.PI*2,0.06+RNG.nextDouble()*0.12});
        for (double[] f : fogPuffs) {
            f[0]+=f[6]; f[5]+=0.003;
            double yW=Math.sin(f[5])*18;
            RadialGradient fg=new RadialGradient(0,0,f[0],f[1]+yW,f[2]/2,false,CycleMethod.NO_CYCLE,
                    new Stop(0,Color.rgb(200,210,220,f[4])),new Stop(1,Color.TRANSPARENT));
            gc.setFill(fg); gc.fillRect(f[0]-f[2]/2,f[1]-f[3]/2,f[2],f[3]);
            if (f[0]>canvasW+f[2]/2) f[0]=-f[2]/2;
        }
    }

    private void drawClouds(GraphicsContext gc) {
        while (clouds.size()<5)
            clouds.add(new double[]{RNG.nextDouble()*canvasW,60+RNG.nextDouble()*160,
                    200+RNG.nextDouble()*280,80+RNG.nextDouble()*80,
                    0.08+RNG.nextDouble()*0.1,0.15+RNG.nextDouble()*0.3});
        for (double[] c : clouds) {
            gc.setFill(Color.rgb(255,255,255,c[4]));
            gc.fillOval(c[0]-c[2]/2,c[1]-c[3]/2,c[2],c[3]);
            gc.fillOval(c[0]-c[2]*0.35,c[1]+c[3]*0.05,c[2]*0.7,c[3]*0.85);
            gc.fillOval(c[0]+c[2]*0.1,c[1]+c[3]*0.1,c[2]*0.55,c[3]*0.75);
            c[0]+=c[5];
            if (c[0]>canvasW+c[2]/2) c[0]=-c[2]/2;
        }
    }

    private void drawSun(GraphicsContext gc) {
        double cx = canvasW * 0.82, cy = canvasH * 0.13;
        // Pulsing glow using sine wave — smooth, no flicker
        double pulse = 0.35 + 0.08 * Math.sin(frameCount * 0.04);
        RadialGradient g = new RadialGradient(0, 0, cx, cy, canvasW * 0.3, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 220, 80, pulse)),
                new Stop(0.4, Color.rgb(255, 200, 50, pulse * 0.4)),
                new Stop(1, Color.TRANSPARENT));
        gc.setFill(g);
        gc.fillOval(cx - canvasW * 0.3, cy - canvasW * 0.3, canvasW * 0.6, canvasW * 0.6);
        gc.setFill(Color.rgb(255, 230, 80, 0.92));
        gc.fillOval(cx - 55, cy - 55, 110, 110);
    }

    private void resetParticles() {
        rainDrops.clear();snowFlakes.clear();heatWaves.clear();
        windLines.clear();fogPuffs.clear();clouds.clear();
        lightningTimer=0;lightningFlash=0;
    }

    private void applyBackground(String type) {
        String grad = switch (type) {
            case "hot"   -> "linear-gradient(to bottom right,#7a1500,#c94010,#f07020,#f5a030)";
            case "rain"  -> "linear-gradient(to bottom right,#1a2a3a,#2a3a4a,#3a5060)";
            case "storm" -> "linear-gradient(to bottom right,#0d0d1a,#1a1030,#2d1a50)";
            case "snow"  -> "linear-gradient(to bottom right,#0d1a2e,#1a2d4a,#2a4a7a,#4a6a9a)";
            case "cloudy"-> "linear-gradient(to bottom right,#2a3a4a,#3a4a5a,#5a6a7a)";
            case "fog"   -> "linear-gradient(to bottom right,#2a2a35,#4a4a55,#6a6a75)";
            case "windy" -> "linear-gradient(to bottom right,#0a2a1a,#1a4a2a,#2a6a4a)";
            default      -> "linear-gradient(to bottom right,#1a6bb5,#3a9bd5,#f7c948)";
        };
        rootPane.setStyle("-fx-background-color:"+grad+";");
    }

    @FXML private void handleSearch() {
        String city = searchField.getText().trim();
        if (!city.isEmpty()) loadWeather(city);
    }

    @FXML private void handleBack() {
        if (animTimer!=null) animTimer.stop();
        SceneManager.switchTo(backButton,"/fxml/Dashboard.fxml");
    }

    private void loadWeather(String city) {
        statusLabel.setText("Loading weather for "+city+"...");
        new Thread(()->{
            try {
                String enc=URLEncoder.encode(city,StandardCharsets.UTF_8);
                String url="https://api.weatherapi.com/v1/forecast.json?key="+API_KEY
                        +"&q="+enc+"&days=3&aqi=yes&alerts=no";
                JSONObject root=new JSONObject(makeApiCall(url));
                JSONObject loc=root.getJSONObject("location");
                JSONObject cur=root.getJSONObject("current");
                JSONObject airQ=cur.optJSONObject("air_quality");
                JSONArray  fcDays=root.getJSONObject("forecast").getJSONArray("forecastday");
                JSONArray  hours=fcDays.getJSONObject(0).getJSONArray("hour");

                String cityName=loc.getString("name");
                String country=loc.getString("country");
                double tempC=cur.getDouble("temp_c");
                double feelsLike=cur.getDouble("feelslike_c");
                String condText=cur.getJSONObject("condition").getString("text");
                double windKph=cur.getDouble("wind_kph");
                int    humidity=cur.getInt("humidity");
                double uv=cur.getDouble("uv");
                double dew=cur.optDouble("dewpoint_c",0);
                double vis=cur.getDouble("vis_km");
                double pres=cur.getDouble("pressure_mb");

                Platform.runLater(()->{
                    updateHero(cityName,country,tempC,feelsLike,condText,windKph,humidity);
                    updateStats(windKph,humidity,uv,dew,vis,pres);
                    updateHourly(hours);
                    updateForecast(fcDays);
                    if (airQ!=null) updateAQ(airQ);
                    statusLabel.setText("Updated for "+cityName);
                });
            } catch(Exception e){
                Platform.runLater(()->statusLabel.setText("Failed: "+e.getMessage()));
            }
        }).start();
    }

    private void updateHero(String city,String country,double temp,double feels,
                            String cond,double wind,int hum){
        cityNameLabel.setText(city); countryLabel.setText(country);
        tempLabel.setText(String.format("%.0f°C",temp));
        feelsLikeLabel.setText(String.format("Feels like: %.0f°C",feels));
        conditionLabel.setText(cond);
        String lower=cond.toLowerCase();
        String icon,tip,type;
        if (lower.contains("snow") || lower.contains("blizzard") || temp < 0) {
            icon = "❄️"; type = "snow"; tip = "❄️ Freezing — dress warmly!";
        } else if (lower.contains("thunder") || lower.contains("storm")) {
            icon = "⛈️"; type = "storm"; tip = "⚡ Thunderstorm — stay indoors!";
        } else if (lower.contains("rain") || lower.contains("drizzle")) {
            icon = "🌧️"; type = "rain"; tip = "🌂 Carry an umbrella!";
        } else if (lower.contains("fog") || lower.contains("mist")) {
            icon = "🌫️"; type = "fog"; tip = "🚗 Low visibility — drive slow";
        } else if (lower.contains("cloud") || lower.contains("overcast")) {
            icon = "⛅"; type = "cloudy"; tip = "🧥 Light jacket recommended";
        } else if (wind > 40 || lower.contains("wind")) {
            icon = "💨"; type = "windy"; tip = "💨 Strong winds — secure loose objects";
        } else if (temp > 38) {
            icon = "🔥"; type = "hot"; tip = "🔥 Extreme heat — stay hydrated!";
        } else if (temp < 10 && (lower.contains("clear") || lower.contains("sunny"))) {
            icon = "🌤️"; type = "cloudy"; tip = "🧥 Cool and clear — jacket recommended";
        } else if (lower.contains("sunny") || lower.contains("clear")) {
            icon = "☀️"; type = "sunny"; tip = "😎 Clear skies — enjoy your day!";
        } else {
            icon = "🌤️"; type = "sunny"; tip = "🌤️ Pleasant weather";
        }
        weatherIconLabel.setText(icon); tipLabel.setText(tip);
        weatherType=type; resetParticles(); applyBackground(type);
    }

    private void updateStats(double wind,int hum,double uv,double dew,double vis,double pres){
        windLabel.setText(String.format("%.0f km/h",wind));
        humidityLabel.setText(hum+"%");
        uvLabel.setText(String.format("%.0f",uv));
        uvDescLabel.setText(uv<=2?"Low":uv<=5?"Moderate":uv<=7?"High":uv<=10?"Very High":"Extreme");
        dewPointLabel.setText(String.format("%.0f°C",dew));
        visibilityLabel.setText(String.format("%.0f km",vis));
        pressureLabel.setText(String.format("%.0f mb",pres));
    }

    private void updateHourly(JSONArray hours){
        hourlyBox.getChildren().clear();
        int now=java.time.LocalTime.now().getHour(), count=0;
        for(int i=0;i<hours.length()&&count<8;i++){
            JSONObject h=hours.getJSONObject(i);
            int hour=Integer.parseInt(h.getString("time").split(" ")[1].split(":")[0]);
            if(hour<now) continue; count++;
            double t=h.getDouble("temp_c");
            String ic=smallIcon(h.getJSONObject("condition").getString("text").toLowerCase(),t);
            int rain=h.getInt("chance_of_rain");
            VBox card=new VBox(4); card.setAlignment(javafx.geometry.Pos.CENTER);
            card.setStyle("-fx-background-color:rgba(255,255,255,0.12);-fx-background-radius:14px;-fx-border-color:rgba(255,255,255,0.18);-fx-border-radius:14px;-fx-border-width:1px;-fx-padding:12px 14px;-fx-min-width:72px;");
            Label l1=new Label(hour+":00");l1.setStyle("-fx-text-fill:rgba(255,255,255,0.65);-fx-font-size:11px;");
            Label l2=new Label(ic);l2.setStyle("-fx-font-size:22px;");
            Label l3=new Label(String.format("%.0f°",t));l3.setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:14px;");
            Label l4=new Label("💧"+rain+"%");l4.setStyle("-fx-text-fill:rgba(100,200,255,0.85);-fx-font-size:10px;");
            card.getChildren().addAll(l1,l2,l3,l4);
            hourlyBox.getChildren().add(card);
        }
    }

    private void updateForecast(JSONArray days){
        forecastBox.getChildren().clear();
        String[] names={"Today","Tomorrow","Day 3"};
        for(int i=0;i<Math.min(3,days.length());i++){
            JSONObject d=days.getJSONObject(i).getJSONObject("day");
            double max=d.getDouble("maxtemp_c"),min=d.getDouble("mintemp_c");
            String cv=d.getJSONObject("condition").getString("text");
            int rain=d.getInt("daily_chance_of_rain");
            VBox card=new VBox(8); card.setAlignment(javafx.geometry.Pos.CENTER);
            card.setStyle("-fx-background-color:rgba(255,255,255,0.09);-fx-background-radius:18px;-fx-border-color:rgba(255,255,255,0.18);-fx-border-radius:18px;-fx-border-width:1px;-fx-padding:18px 20px;");
            card.setPrefWidth(200);
            Label n=new Label(names[i]);n.setStyle("-fx-text-fill:rgba(255,255,255,0.75);-fx-font-weight:bold;-fx-font-size:13px;");
            Label ic=new Label(smallIcon(cv.toLowerCase(),max));ic.setStyle("-fx-font-size:36px;");
            Label co=new Label(cv);co.setStyle("-fx-text-fill:rgba(255,255,255,0.85);-fx-font-size:12px;");co.setWrapText(true);co.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            Label tr=new Label(String.format("%.0f° / %.0f°C",max,min));tr.setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:15px;");
            Label rl=new Label("💧 Rain: "+rain+"%");rl.setStyle("-fx-text-fill:rgba(100,200,255,0.85);-fx-font-size:11px;");
            card.getChildren().addAll(n,ic,co,tr,rl);
            forecastBox.getChildren().add(card);
        }
    }

    private void updateAQ(JSONObject aq){
        double pm25=aq.optDouble("pm2_5",0),pm10=aq.optDouble("pm10",0);
        double o3=aq.optDouble("o3",0),no2=aq.optDouble("no2",0);
        double so2=aq.optDouble("so2",0),co=aq.optDouble("co",0);
        int idx=aq.optInt("us-epa-index",1);
        String[] tx={"","Good","Moderate","Unhealthy for Sensitive","Unhealthy","Very Unhealthy","Hazardous"};
        String[] cl={"","#00e400","#ffff00","#ff7e00","#ff0000","#8f3f97","#7e0023"};
        aqiLabel.setText("AQI: "+idx);
        aqiStatusLabel.setText(idx>=1&&idx<=6?tx[idx]:"Unknown");
        aqiStatusLabel.setStyle("-fx-text-fill:"+(idx>=1&&idx<=6?cl[idx]:"#fff")+";-fx-font-weight:bold;-fx-font-size:13px;");
        pm25Label.setText(String.format("%.1f μg/m³",pm25));
        pm10Label.setText(String.format("%.1f μg/m³",pm10));
        o3Label.setText(String.format("%.1f μg/m³",o3));
        no2Label.setText(String.format("%.1f μg/m³",no2));
        so2Label.setText(String.format("%.1f μg/m³",so2));
        coLabel.setText(String.format("%.1f μg/m³",co));
        pm25Bar.setProgress(Math.min(pm25/250.0,1.0));
        pm10Bar.setProgress(Math.min(pm10/430.0,1.0));
        o3Bar.setProgress(Math.min(o3/200.0,1.0));
        no2Bar.setProgress(Math.min(no2/200.0,1.0));
    }

    private String smallIcon(String c,double t){
        if(c.contains("snow")||c.contains("blizzard"))return "❄️";
        if(c.contains("thunder")||c.contains("storm"))return "⛈️";
        if(c.contains("rain")||c.contains("drizzle"))return "🌧️";
        if(c.contains("cloud")||c.contains("overcast"))return "⛅";
        if(c.contains("fog")||c.contains("mist"))return "🌫️";
        if(c.contains("wind"))return "💨";
        if(t>38)return "🔥";
        if(c.contains("sunny")||c.contains("clear"))return "☀️";
        return "🌤️";
    }

    private String makeApiCall(String u) throws Exception {
        URL url=URI.create(u).toURL();
        HttpURLConnection c=(HttpURLConnection)url.openConnection();
        c.setRequestMethod("GET");c.setRequestProperty("Accept","application/json");
        c.setRequestProperty("User-Agent","NADAS/1.0");
        c.setConnectTimeout(8000);c.setReadTimeout(8000);
        BufferedReader in=new BufferedReader(new InputStreamReader(c.getInputStream()));
        StringBuilder sb=new StringBuilder();String line;
        while((line=in.readLine())!=null)sb.append(line);
        in.close();return sb.toString();
    }
}
