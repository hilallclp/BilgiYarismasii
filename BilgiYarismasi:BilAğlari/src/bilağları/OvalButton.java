package bilağları;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JButton;

class OvalButton extends JButton {
    public OvalButton(Icon icon) {
        super(icon);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isArmed()) {
            g.setColor(new Color(0, 102, 204));
        } else {
            g.setColor(new Color(0, 51, 102));
        }
        g.fillOval(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(Color.YELLOW);
        g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
    }
}

