import javax.swing.*;

public class Main extends JFrame {
    private ControlWindow cw = new ControlWindow();
    public Main(){
        this.setTitle("Akarnoid");
        this.setSize(ControlWindow.WIDTH, ControlWindow.HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
        this.add(cw);
        //this.pack();
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new Menu().setVisible(true);
        });
    }
}
