import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ViewFrame extends JFrame {
    
    private VisualiserPanel panel;
    private ViewModel model;
    private RenderSynch sync;
    
    public ViewFrame(ViewModel model, int w, int h){
        this.model = model;
        this.sync = new RenderSynch();
        setTitle("Poool Game");
        setSize(w, h + 25);
        setResizable(false);
        panel = new VisualiserPanel(w, h);
        getContentPane().add(panel);
        addWindowListener(new WindowAdapter(){
          public void windowClosing(WindowEvent ev){
             System.exit(-1);
          }
        });
    }
     
    public void render(){
        long nf = sync.nextFrameToRender();
        panel.repaint();
        try {
           sync.waitForFrameRendered(nf);
        } catch (InterruptedException ex) {
           ex.printStackTrace();
        }
    }
        
    public class VisualiserPanel extends JPanel {
        private int ox;
        private int oy;
        private int delta;
        
        public VisualiserPanel(int w, int h){
            setSize(w, h + 25);
            ox = w / 2;
            oy = h / 2;
            // 1.0 nel modello corrisponde a delta pixel a schermo
            delta = Math.min(ox, oy);
        }

        @Override
        public void paintComponent(Graphics g) {
           super.paintComponent(g);
           Graphics2D g2 = (Graphics2D) g;
           
           g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
           g2.clearRect(0, 0, getWidth(), getHeight());
            
           // Buchi negli angoli alti
           g2.setColor(Color.BLACK);
           int holeRadius = 25;
           g2.fillOval(-holeRadius, -holeRadius, holeRadius * 2, holeRadius * 2);
           g2.fillOval(getWidth() - holeRadius, -holeRadius, holeRadius * 2, holeRadius * 2);

           // Assi di riferimento
           g2.setColor(Color.LIGHT_GRAY);
           g2.setStroke(new BasicStroke(1));
           g2.drawLine(ox, 0, ox, getHeight());
           g2.drawLine(0, oy, getWidth(), oy);
           
           // Punteggi grandi blu
           g2.setColor(Color.BLUE);
           g2.setFont(new Font("Arial", Font.PLAIN, 100));
           g2.drawString(String.valueOf(model.getHumanScore()), ox / 6, oy + 140);
           g2.drawString(String.valueOf(model.getBotScore()), (int) (ox * 1.67), oy + 140);

           // Piccole palline
           g2.setColor(Color.BLACK);
           g2.setStroke(new BasicStroke(1));
           for (var b : model.getBalls()) {
               var p = b.pos();
               int x0 = (int) (ox + p.x() * delta);
               int y0 = (int) (oy - p.y() * delta);
               int r = (int) (b.radius() * delta);
               g2.drawOval(x0 - r, y0 - r, r * 2, r * 2);
           }

           // Pallina umana
           var humanBall = model.getHumanBall();
           if (humanBall != null) {
               drawPlayerBall(g2, humanBall, "H");
           }

           // Pallina bot
           var botBall = model.getBotBall();
           if (botBall != null) {
               drawPlayerBall(g2, botBall, "B");
           }
           
           // Debug info
           g2.setFont(new Font("Arial", Font.PLAIN, 12));
           g2.setColor(Color.BLACK);
           g2.drawString("Balls: " + model.getBalls().size(), 20, 20);
           g2.drawString("FPS: " + model.getFramePerSec(), 20, 35);

           sync.notifyFrameRendered();
        }

        private void drawPlayerBall(Graphics2D g2, BallViewInfo ball, String label) {
            var p = ball.pos();
            int x0 = (int) (ox + p.x() * delta);
            int y0 = (int) (oy - p.y() * delta);
            int r = (int) (ball.radius() * delta);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x0 - r, y0 - r, r * 2, r * 2);

            g2.setFont(new Font("Arial", Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            int textX = x0 - (fm.stringWidth(label) / 2);
            g2.drawString(label, textX, y0 + 5);
        }
    }
}
