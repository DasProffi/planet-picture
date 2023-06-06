import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GUI extends JFrame {
    // UI Elements
    private final JLabel lSeed = new JLabel();
    private final JLabel jLabelSeedNumber = new JLabel();
    private final JLabel jLabelPicture = new JLabel();
    private final JLabel jLabelFilename = new JLabel();
    private final JLabel jLabelDirectory = new JLabel();
    private final JLabel jLabelSave = new JLabel();
    private final JButton jButtonReroll = new JButton();
    private final JButton jButtonGenerateBySeed = new JButton();
    private final JButton jButtonSavePicture = new JButton();
    private final TextField jTextFieldSeed = new TextField();
    private final TextField jTextFieldFilename = new TextField();
    private final TextField jTextFieldDirectory = new TextField();

    private final ImageGenerator planet = new ImageGenerator();

    public GUI() {
        // Frame-Initialization
        super();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        int frameWidth = 1250;
        int frameHeight = 1070;
        setSize(frameWidth, frameHeight);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (d.width - getSize().width) / 2;
        int y = (d.height - getSize().height) / 2;
        setLocation(x, y);
        setTitle("Planet-Image");
        setResizable(false);
        Container cp = getContentPane();
        cp.setLayout(null);

        // Add Components to Container
        lSeed.setBounds(1050, 25, 50, 20);
        lSeed.setText("Seed:");
        cp.add(lSeed);

        planet.rerollSeed();

        jLabelSeedNumber.setBounds(1125, 25, 75, 20);
        jLabelSeedNumber.setText("" + planet.getSeed());
        cp.add(jLabelSeedNumber);

        jLabelPicture.setBounds(25, 25, 1000, 1000);
        jLabelPicture.setText("");
        ImageIcon icon = new ImageIcon(planet.makeImg());
        jLabelPicture.setIcon(icon);
        cp.add(jLabelPicture);

        jLabelSave.setBounds(1050, 250, 150, 20);
        jLabelSave.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelSave.setText("Save File");
        cp.add(jLabelSave);

        jLabelFilename.setBounds(1050, 270, 150, 20);
        jLabelFilename.setText("Filename");
        cp.add(jLabelFilename);

        jLabelDirectory.setBounds(1050, 320, 200, 20);
        jLabelDirectory.setText("Directory (Absolute Path)");
        cp.add(jLabelDirectory);

        jTextFieldFilename.setBounds(1050, 290, 150, 25);
        jTextFieldFilename.setText("planet_picture_" + planet.getSeed());
        cp.add(jTextFieldFilename);

        jTextFieldDirectory.setBounds(1050, 340, 150, 25);
        jTextFieldDirectory.setText("C:\\Pictures");
        cp.add(jTextFieldDirectory);

        jTextFieldSeed.setBounds(1050, 150, 150, 25);
        jTextFieldSeed.setText("");
        cp.add(jTextFieldSeed);

        jButtonReroll.setBounds(1050, 75, 150, 25);
        jButtonReroll.setText("New Seed");
        jButtonReroll.setMargin(new Insets(2, 2, 2, 2));
        jButtonReroll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                rerollSeed();
            }
        });
        cp.add(jButtonReroll);

        jButtonSavePicture.setBounds(1050, 370, 150, 25);
        jButtonSavePicture.setText("Save File");
        jButtonSavePicture.setMargin(new Insets(2, 2, 2, 2));
        jButtonSavePicture.addActionListener(evt -> savePicture());
        cp.add(jButtonSavePicture);

        jButtonGenerateBySeed.setBounds(1050, 200, 150, 25);
        jButtonGenerateBySeed.setText("New with this Seed");
        jButtonGenerateBySeed.setMargin(new Insets(2, 2, 2, 2));
        jButtonGenerateBySeed.addActionListener(evt -> generateWithSeed());
        cp.add(jButtonGenerateBySeed);

        setVisible(true);
    }

    public static void main(String[] args) {
        new GUI();
    }

    public void savePicture() {
        planet.imgToFile(planet.createBySeed(Integer.parseInt(jLabelSeedNumber.getText())),
                jTextFieldDirectory.getText(), jTextFieldFilename.getText()
        );
    }

    public void rerollSeed() {
        planet.rerollSeed();
        ImageIcon icon = new ImageIcon(planet.makeImg());
        updateUI(icon);
    }

    public void generateWithSeed() {
        int seed = Integer.parseInt(jTextFieldSeed.getText());
        if (seed < Math.pow(10, 8)) {
            ImageIcon icon = new ImageIcon(planet.createBySeed(seed));
            updateUI(icon);
        }
    }

    private void updateUI(ImageIcon icon) {
        jLabelPicture.setIcon(icon);
        jLabelSeedNumber.setText("" + planet.getSeed());
        jTextFieldFilename.setText("planet_picture_" + planet.getSeed());
        jTextFieldSeed.setText("" + planet.getSeed());
    }
}
