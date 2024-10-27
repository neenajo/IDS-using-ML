import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

class IntruderDetectionGUI extends JFrame {
    private JTextArea logTextArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton displayAttacksButton;
    private boolean isRunning;
    private Thread idsThread;

    private int networkActivityThreshold = 70;

    private List<Intrusion> attackHistory;
    private List<Integer> networkActivityLevels; 
    private int windowSize = 10; 

    public IntruderDetectionGUI() {
        setTitle("Intruder Detection System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(Color.BLACK); 
        logTextArea.setForeground(Color.WHITE); 
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);

        startButton = new JButton("Start IDS");
        startButton.setBackground(Color.BLACK); 
        startButton.setForeground(Color.WHITE); 
        stopButton = new JButton("Stop IDS");
        stopButton.setBackground(Color.BLACK); 
        stopButton.setForeground(Color.WHITE); 
        displayAttacksButton = new JButton("Display Attacks");
        displayAttacksButton.setBackground(Color.BLACK); 
        displayAttacksButton.setForeground(Color.WHITE); 

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(displayAttacksButton);

        isRunning = false;
        attackHistory = new ArrayList<>();
        networkActivityLevels = new ArrayList<>(); 

        GraphPanel graphPanel = new GraphPanel();

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isRunning) {
                    logTextArea.append("INTRUDER DETECTION SYSTEM STARTED\n");
                    isRunning = true;
                    idsThread = new Thread(new IDSRunnable(graphPanel));
                    idsThread.start();
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isRunning) {
                    logTextArea.append("INTRUDER DETECTION SYSTEM STOPPED\n");
                    isRunning = false;
                    idsThread.interrupt();
                }
            }
        });

        displayAttacksButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logTextArea.append("Attack History:\n");
                for (Intrusion intrusion : attackHistory) {
                    logTextArea.append("Type: " + intrusion.getType() + ", Severity: " + intrusion.getSeverity() + ", Remedy: " + intrusion.getRemedy() + "\n");
                }
            }
        });

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(graphPanel, BorderLayout.NORTH); 
    }


    class GraphPanel extends JPanel {
        public GraphPanel() {
            // Constructor
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();
            
            g.setColor(Color.BLACK);
            g.drawLine(50, height - 50, width - 50, height - 50);
            g.drawLine(50, 50, 50, height - 50);
            
            g.setColor(Color.BLUE);
            for (int i = 1; i < networkActivityLevels.size(); i++) {
                int x1 = 50 + (i - 1) * (width - 100) / (networkActivityLevels.size() - 1);
                int y1 = height - 50 - networkActivityLevels.get(i - 1);
                int x2 = 50 + i * (width - 100) / (networkActivityLevels.size() - 1);
                int y2 = height - 50 - networkActivityLevels.get(i);
                g.drawLine(x1, y1, x2, y2);
            }
            
            g.setColor(Color.RED);
            for (int i = 0; i < networkActivityLevels.size(); i++) {
                int x = 50 + i * (width - 100) / (networkActivityLevels.size() - 1);
                int y = height - 50 - networkActivityLevels.get(i);
                g.fillOval(x - 2, y - 2, 6, 6);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, 300); 
        }
    }


    class IDSRunnable implements Runnable {
        private GraphPanel graphPanel;

        public IDSRunnable(GraphPanel graphPanel) {
            this.graphPanel = graphPanel;
        }

        public void run() {
            Random random = new Random();
            while (isRunning) {
                int networkActivity = random.nextInt(100);
                logTextArea.append("Current Traffic: " + networkActivity + "\n");
                updateNetworkActivity(networkActivity); 

                if (networkActivity > networkActivityThreshold) {
                    Intrusion intrusion = detectIntrusionType();
                    logTextArea.append("Security Alert / Intrusion detected: " + intrusion.getType() + ", Severity: " + intrusion.getSeverity() + "\n");
                    attackHistory.add(intrusion);
                }
                

                if (networkActivityLevels.size() >= windowSize) {
                    detectAnomalies();
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        private Intrusion detectIntrusionType() {
            Random random = new Random();
            int type = random.nextInt(3);
            String intrusionType;
            int severity;
            String remedy;
            switch (type) {
                case 0:
                    intrusionType = "Malware Infection";
                    severity = 1;
                    remedy = "Isolate the infected system and run an antivirus scan.";
                    break;
                case 1:
                    intrusionType = "Brute Force Attack";
                    severity = 2;
                    remedy = "Block the source IP address and implement account lockout policies.";
                    break;
                case 2:
                    intrusionType = "DDoS Attack";
                    severity = 3;
                    remedy = "Implement traffic filtering and request rate limiting.";
                    break;
                default:
                    intrusionType = "Unknown Intrusion";
                    severity = 0;
                    remedy = "Unknown intrusion type. Consult with a security expert.";
            }
            LocalTime time = LocalTime.now(); 
            return new Intrusion(intrusionType, severity, remedy, time);  
        }

        private void updateNetworkActivity(int level) {
            networkActivityLevels.add(level);
            graphPanel.repaint(); 
        }

        private void detectAnomalies() {
            int sum = 0;
            int sumSquared = 0;
            for (int level : networkActivityLevels) {
                sum += level;
                sumSquared += level * level;
            }
            double mean = (double) sum / networkActivityLevels.size();
            double variance = (double) sumSquared / networkActivityLevels.size() - mean * mean;
            double stdDev = Math.sqrt(variance);
            double threshold = mean + 2 * stdDev; 

            for (int level : networkActivityLevels) {
                if (level > threshold) {
                    logTextArea.append("Anomaly detected: Network activity level " + level + " exceeds threshold.\n");
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                IntruderDetectionGUI gui = new IntruderDetectionGUI();
                gui.setVisible(true);
            }
        });
    }
}

class Intrusion {
    private String type;
    private int severity;
    private String remedy;
    private LocalTime time;

    public Intrusion(String type, int severity, String remedy, LocalTime time) {
        this.type = type;
        this.severity = severity;
        this.remedy = remedy;
        this.time = time;
    }

    public Intrusion(String type, int severity, String remedy) {
        this.type = type;
        this.severity = severity;
        this.remedy = remedy;
    }

    public String getType() {
        return type;
    }

    public int getSeverity() {
        return severity;
    }

    public String getRemedy() {
        return remedy;
    }

    public LocalTime getTime() {
        return time;
    }
}


