/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cs.chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author s506571
 */
public class ClientFrame extends JFrame {
    JTextPane messages;
    StyledDocument msgDoc;
    JList userList;
    public DefaultListModel users;
    JTextField msgText;
    JButton sendBtn;
    MutableAttributeSet userAS;
    ClientListener listener;
    JScrollPane msgScroll;
    public ClientFrame() {
        super("Chat");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Container cp = this.getContentPane();
        cp.setLayout(new BorderLayout());
        ((BorderLayout)cp.getLayout()).setHgap(5);
        messages = new JTextPane();
        messages.setPreferredSize(new Dimension(500, 400));
        messages.setEditable(false);
        ((DefaultCaret)messages.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        msgDoc = messages.getStyledDocument();
        msgScroll = new JScrollPane(messages);
        cp.add(msgScroll, BorderLayout.CENTER);
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());
        users = new DefaultListModel();
        userList = new JList(users);
        userList.setMinimumSize(new Dimension(150, 20));
        userList.setPreferredSize(new Dimension(150, 400));
        userPanel.add(userList, BorderLayout.CENTER);
        userPanel.add(new JLabel("USERS"), BorderLayout.PAGE_START);
        cp.add(userPanel, BorderLayout.LINE_END);
        JPanel tpanel = new JPanel();
        tpanel.setLayout(new BorderLayout());
        cp.add(tpanel, BorderLayout.PAGE_END);
        msgText = new JTextField();
        tpanel.add(msgText, BorderLayout.CENTER);
        sendBtn = new JButton();
        tpanel.add(sendBtn, BorderLayout.LINE_END);
        sendBtn.setText("SEND");
        userAS = new SimpleAttributeSet();
        StyleConstants.setBold(userAS, true);
        StyleConstants.setForeground(userAS, Color.RED);
        listener = null;
        ActionListener sendListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String t = msgText.getText();
                msgText.setText("");
                if (listener != null)
                    listener.messageSend(t);
            }
        };
        sendBtn.addActionListener(sendListener);
        msgText.addActionListener(sendListener);
        pack();
    }

    public void addMessage(String user, String message) {
        try {
            // null is attributeSet
            msgDoc.insertString(msgDoc.getLength(), "<" + user + "> ", userAS);
            msgDoc.insertString(msgDoc.getLength(), message + "\n", null);
        } catch (BadLocationException ex) {
            return;
        }
    }

    public void setListener(ClientListener l) {
        listener = l;
    }

    public static void main(String argv[]) {
        final ClientFrame f = new ClientFrame();
        f.addMessage("ThienSon", "Hello James");
        f.addMessage("James", "Hi");
        f.addMessage("ThienSon", "You're cool");
        f.addMessage("James", "HOK");
        f.setListener(new ClientListener() {
            public void messageSend(String msg) {
                f.addMessage(Math.random() < 0.5 ? "James" : "ThienSon", msg);
            }
        });
        f.setVisible(true);
        f.users.addElement("James");
        f.users.addElement("ThienSon");
    }
}
