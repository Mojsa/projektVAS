/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projektvas;

import io.swagger.client.*;
import io.swagger.client.model.*;
import io.swagger.client.api.DefaultApi;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputListener;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import sample2_waypoints.RoutePainter;

/**
 *
 * @author Mariofil
 */
//View, will show a travel map
public class AgentVAS extends Agent {

    TripData trip;
    List<Double> listWalkTimes = new ArrayList<>();

    protected void setup(TripData data) {
        trip = data;
        addBehaviour(new SingleBehaviour());
    }

    private class SingleBehaviour extends Behaviour {

        @Override
        public void action() {
            myAgent.addBehaviour(new OneShotBehaviour(myAgent) {
                @Override
                public void action() {
                    try {
                        odradi();
                    } catch (IOException ex) {
                        Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }

        public boolean done() {
            return true;
        }
    }

    /**
     * @param args the command line arguments
     */
    public void odradi() throws MalformedURLException, IOException {
        File temp = new File(Paths.get("").toAbsolutePath().normalize() + File.separator + "hehe.bin");
        if (!temp.exists()) {
            temp.createNewFile();
        } else if (temp.exists()) {

            ObjectInputStream ois = null;
            FileInputStream streamIn = null;
            try {
                streamIn = new FileInputStream(temp);
                ois = new ObjectInputStream(streamIn);
                List<Double> listaProba = (List<Double>) ois.readObject();
                if (listaProba != null) {
                    listWalkTimes.addAll(listaProba);
                }
            } catch (Exception ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (streamIn != null) {
                    streamIn.close();
                }
                if (ois != null) {
                    ois.close();
                }
            }
        }
        HashMap<String, String> flightHotelMap = new HashMap<>();
        JXMapViewer mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        tileFactory.setThreadPoolSize(8);

        JSONObject startObj = getLatLong(trip.getStartPoint());
        JSONObject destinationObj = getLatLong(trip.getDestinationPoint());

        GeoPosition startPositon = new GeoPosition(startObj.getDouble("lat"), startObj.getDouble("lng"));
        GeoPosition destinationPosition = new GeoPosition(destinationObj.getDouble("lat"), destinationObj.getDouble("lng"));

        List<GeoPosition> track = Arrays.asList(startPositon, destinationPosition);
        RoutePainter routePainter = new RoutePainter(track);

        mapViewer.zoomToBestFit(new HashSet<GeoPosition>(track), 0.7);

        //new waypoints
        Comparator<MyWaypoint> byLabel = (MyWaypoint w1, MyWaypoint w2) -> Integer.compare(Integer.valueOf(w1.getLabel()), Integer.valueOf(w2.getLabel()));
        TreeSet<MyWaypoint> waypoints = new TreeSet<MyWaypoint>(byLabel);
        waypoints.add(new MyWaypoint("1", "Start: " + trip.getStartPoint(), Color.RED, startPositon));
        waypoints.add(new MyWaypoint("2", "Destination: " + trip.getDestinationPoint(), Color.BLUE, destinationPosition));
        WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(new FancyWaypointRenderer());

        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);
        painters.add(waypointPainter);
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);
        mapViewer.setAddressLocation(startPositon);

        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        JPanel p = new JPanel();
        JButton b = new JButton("Show waypoints and trip data.");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new TripDataWayPointsFrame(trip, waypoints, flightHotelMap).setVisible(true);
                    }
                });
            }
        });
        p.add(b);
        // Display the viewer in a JFrame
        JFrame frame = new JFrame("Trip Map");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(p, BorderLayout.NORTH);
        frame.getContentPane().add(mapViewer);
        frame.setSize(800, 600);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                ObjectOutputStream oos = null;
                FileOutputStream fout = null;
                try {
                    fout = new FileOutputStream(temp);
                    oos = new ObjectOutputStream(fout);
                    oos.writeObject(listWalkTimes);
                } catch (Exception ex) {
                    Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (oos != null) {
                        try {
                            oos.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (fout != null) {
                        try {
                            fout.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });

        mapViewer.addPropertyChangeListener("zoom", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateWindowTitle(frame, mapViewer);
            }
        });

        mapViewer.addPropertyChangeListener("center", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateWindowTitle(frame, mapViewer);
            }
        });
        DefaultApi apiInstance = new DefaultApi();
        String apikey = "v9rayKgihnn0PyGcyX1JbrnEAAkqcY2k"; // String | API Key provided for your account, to identify you for API access. Make sure to keep this API key secret.
        Boolean allAirports = false; // Boolean | Boolean to include or not all airports, no matter their traffic rank. False by default, to only display relevant airports.
        try {
            //get IATA codes for starting and destination
            List<NearestAirport> response = apiInstance.nearestRelevantAirport(apikey, String.valueOf(startObj.getDouble("lat")), String.valueOf(startObj.getDouble("lng")));
            NearestAirport startAirport = response.get(0);
            List<NearestAirport> response2 = apiInstance.nearestRelevantAirport(apikey, String.valueOf(destinationObj.getDouble("lat")), String.valueOf(destinationObj.getDouble("lng")));
            NearestAirport destinationAirport = response2.get(0);
            //flight inspiration, extensive, low-fare or affiliate search by iata code, start and end date
            LowFareSearchResponse lfsr = apiInstance.flightLowFareSearch(apikey, startAirport.getCity(), destinationAirport.getCity(), trip.getStartDate(), null, null, null, null, null, null, null, null, null, null, null, null, null);
            LowFareSearchResult result3 = lfsr.getResults().stream().min((p1, p2) -> Double.compare(Double.valueOf(p1.getFare().getTotalPrice()), Double.valueOf(p2.getFare().getTotalPrice()))).orElse(null);

            flightHotelMap.put("flightPrice", result3.getFare().getTotalPrice());
            flightHotelMap.put("departsFromStart", result3.getItineraries().get(0).getOutbound().getFlights().get(0).getDepartsAt());
            flightHotelMap.put("arrivesAtDestination", result3.getItineraries().get(0).getOutbound().getFlights().get(0).getArrivesAt());
            flightHotelMap.put("durationToDestination", result3.getItineraries().get(0).getOutbound().getDuration());

            //hotel geosearch by circle,box or by airport??
            HotelSearchResponse hsr = apiInstance.hotelGeosearchByCircle(apikey, BigDecimal.valueOf(destinationObj.getDouble("lat")), BigDecimal.valueOf(destinationObj.getDouble("lng")), 42, trip.getStartDate(), trip.getEndDate(), null, null, null, null, null, null, null, null);

            HotelPropertyResponse hresult = hsr.getResults().stream().min((p1, p2) -> Double.compare(Double.valueOf(p1.getTotalPrice().getAmount()), Double.valueOf(p2.getTotalPrice().getAmount()))).orElse(null);
            flightHotelMap.put("hotelAddress", hresult.getAddress().getCity() + ", " + hresult.getAddress().getLine1());
            flightHotelMap.put("hotelExpenses", hresult.getTotalPrice().getAmount());

            //points of interest
            Double number = listWalkTimes.stream().mapToDouble(i -> i).sum();
            Double average = number / listWalkTimes.size();
            PointsOfInterestResponse poif = apiInstance.yapQGeosearch(apikey, BigDecimal.valueOf(destinationObj.getDouble("lat")), BigDecimal.valueOf(destinationObj.getDouble("lng")), 100, null, null, null, null, null, null, 4, 20);
            poif.getPointsOfInterest().stream().filter((x) -> x.getWalkTime().doubleValue() >= average && x.getWalkTime().doubleValue() <= average * 1.5);
            try {
                JFrame fr = new JFrame("Points of interest");
                int haps = 5, snaps = 4;
                JPanel panel = new JPanel(new GridLayout(haps, snaps, 0, 0));
                JScrollPane scroll = new JScrollPane(panel);
                fr.add(scroll);
                //Layout as a grid with 4 rows and 3 columns
                JLabel labels[] = new JLabel[(haps * snaps)];
                for (int i = 0; i < poif.getPointsOfInterest().size(); i++) {
                    if (i < poif.getPointsOfInterest().size()) {
                        PointOfInterestResult r = poif.getPointsOfInterest().get(i);
                        String nesto = r.getDetails().getShortDescription();
                        labels[i] = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().createImage(new URL(poif.getPointsOfInterest().get(i).getMainImage()))));
                        labels[i].addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent me) {
                                int n = JOptionPane.showConfirmDialog(frame,
                                        "<html><body><p style='width: 200px;'>" + nesto + "</p></body></html>",
                                        "Click yes to add waypoint.",
                                        JOptionPane.YES_NO_OPTION);
                                if (n == JOptionPane.YES_OPTION) {
                                    MyWaypoint wa = waypoints.stream().filter(w -> w.getPosition().equals(new GeoPosition(r.getLocation().getLatitude().doubleValue(), r.getLocation().getLongitude().doubleValue()))).findAny().orElse(null);
                                    if (wa == null) {
                                        listWalkTimes.add(r.getWalkTime().doubleValue());
                                        System.out.println("Adding waypoint.");
                                        waypoints.add(new MyWaypoint(String.valueOf(waypoints.size() + 1), r.getDetails().getShortDescription(), Color.white,
                                                new GeoPosition(r.getLocation().getLatitude().doubleValue(), r.getLocation().getLongitude().doubleValue())));
                                        waypointPainter.setWaypoints(waypoints);
                                        painters.add(waypointPainter);
                                        mapViewer.setOverlayPainter(painter);
                                        JOptionPane.showMessageDialog(frame, "OK.", "Waypoint has been added", JOptionPane.INFORMATION_MESSAGE);
                                        frame.setSize(800, 600);
                                        frame.setPreferredSize(new Dimension(800, 600));
                                        frame.pack();
                                        frame.setVisible(true);
                                    } else {
                                        JOptionPane.showMessageDialog(frame, "Waypoint has already been added, number: " + wa.getLabel(), "Error.", JOptionPane.INFORMATION_MESSAGE);
                                    }
                                }
                            }
                        });
                        panel.add(labels[i]);
                    }
                }
                fr.add(panel);
                fr.pack();
                fr.setSize(600, 600);
                fr.setVisible(true);
            } catch (Exception e) {

            }
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#airportAutocomplete");
            e.printStackTrace();
        }
    }

    protected static void updateWindowTitle(JFrame frame, JXMapViewer mapViewer) {
        double lat = mapViewer.getCenterPosition().getLatitude();
        double lon = mapViewer.getCenterPosition().getLongitude();
        int zoom = mapViewer.getZoom();
        frame.setTitle(String.format("Trip Map Viewer (%.2f / %.2f) - Zoom: %d", lat, lon, zoom));
    }

    private JSONObject getLatLong(String point) throws IOException {
        JSONObject startingPoint = new JSONObject(IOUtils.toString(new URL("http://maps.googleapis.com/maps/api/geocode/json?address=" + point + "&sensor=false"),
                Charset.forName("UTF-8")));
        JSONObject startObj = startingPoint.getJSONArray("results").getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location");
        return startObj;
    }
}
