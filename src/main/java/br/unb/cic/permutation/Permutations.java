package br.unb.cic.permutation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import lombok.val;

public class Permutations {
    private JTextField textField1;
    private JButton OKButton;
    private JTextArea textArea1;
    private JPanel rootPanel;

    public Permutations() {
        OKButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                val permutations = textField1.getText().split("\\)\\(");
                var result = (Permutation) new MulticyclePermutation("(0)");

                for (val p : permutations) {
                    if (p.startsWith("(")) {
                        result = result.times(new MulticyclePermutation(p + ")"));
                    } else if (p.endsWith(")")) {
                        result = result.times(new MulticyclePermutation("(" + p));
                    } else {
                        result = result.times(new MulticyclePermutation("(" + p + ")"));
                    }
                }
                textArea1.append(textField1.getText() + "=" + result.toString() + "\n");

            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Permutations");
        frame.setContentPane(new Permutations().rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
