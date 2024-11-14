package az.mycompany;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Objects;

public class Minesweeper extends JFrame {

    private final JButton[][] buttons;
    private int[][] mines;
    private final int rows, cols, totalMines;

    private JMenu typeMenu;

    private JLabel timerLabel;
    private Timer timer;
    private int elapsedTime = 0;

    private boolean gameOver = false;

    private ImageIcon mineIcon;
    private ImageIcon flagIcon;

    public Minesweeper(int rows, int cols, int totalMines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = totalMines;

        buttons = new JButton[rows][cols];
        mines = new int[rows][cols];

        placeMines(totalMines);
        calculateAdjacentNumbers();

        setLayout(new BorderLayout());
        initializeMenuBar();
        initializeButtons();
        initializeTimer();

        mineIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/mine.png")));
        Image image = mineIcon.getImage();
        Image scaledImage = image.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        mineIcon = new ImageIcon(scaledImage);

        flagIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/flag.png")));
        Image flagImage = flagIcon.getImage();
        Image scaledFlagImage = flagImage.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        flagIcon = new ImageIcon(scaledFlagImage);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(cols * 40, rows * 40 + 80);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = getjMenu();

        typeMenu = new JMenu("Type");

        addTypeMenuItem("9x9 (10 mines)", 9, 9, 10);
        addTypeMenuItem("9x9 (35 mines)", 9, 9, 35);
        addTypeMenuItem("16x16 (40 mines)", 16, 16, 40);
        addTypeMenuItem("16x16 (99 mines)", 16, 16, 99);
        addTypeMenuItem("30x16 (99 mines)", 16, 30, 99);
        addTypeMenuItem("30x16 (70 mines)", 16, 30, 70);
        addCustomTypeMenuItem();

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(typeMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JMenu getjMenu() {
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem restartItem = new JMenuItem("Restart");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");

        newItem.addActionListener(e -> startNewGame());
        restartItem.addActionListener(e -> restartGame());
        saveItem.addActionListener(e -> saveGame());
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newItem);
        gameMenu.add(restartItem);
        gameMenu.add(saveItem);
        gameMenu.add(exitItem);
        return gameMenu;
    }

    private void addTypeMenuItem(String title, int rows, int cols, int mines) {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(e -> startNewGame(rows, cols, mines));
        typeMenu.add(item);
    }

    private void addCustomTypeMenuItem() {
        JMenuItem item = new JMenuItem("Custom");
        item.addActionListener(e -> {
            JTextField rowsField = new JTextField();
            JTextField colsField = new JTextField();
            JTextField minesField = new JTextField();

            Object[] message = {
                    "Rows:", rowsField,
                    "Columns:", colsField,
                    "Mines:", minesField
            };

            int option = JOptionPane.showConfirmDialog(
                    null, message, "Custom Game", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    int r = Integer.parseInt(rowsField.getText());
                    int c = Integer.parseInt(colsField.getText());
                    int m = Integer.parseInt(minesField.getText());
                    if (r > 0 && c > 0 && m > 0 && m < r * c) {
                        startNewGame(r, c, m);
                    } else {
                        JOptionPane.showMessageDialog(
                                null, "Invalid input. Please try again.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            null, "Invalid input. Please enter numeric values.");
                }
            }
        });
        typeMenu.add(item);
    }

    private void startNewGame() {
        dispose();
        new Minesweeper(rows, cols, totalMines);
    }

    private void startNewGame(int newRows, int newCols, int newMines) {
        dispose();
        new Minesweeper(newRows, newCols, newMines);
    }

    private void restartGame() {
        dispose();
        new Minesweeper(rows, cols, totalMines);
    }

    private void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Game");

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(saveFile)) {
                pw.println(rows + " " + cols + " " + totalMines);
                pw.println(elapsedTime);
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        pw.print(mines[i][j] + " ");
                    }
                    pw.println();
                }
                JOptionPane.showMessageDialog(null, "Game saved successfully.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error saving the game.");
            }
        }
    }

    private void showAbout() {
            String message = """
            Minesweeper Game
            Created by Fuad Valiyev.

            Left-click to reveal a cell.
            Right-click to place or remove a flag.

            Avoid the mines and uncover all the cells!

            Enjoy the game!
            """;
            JOptionPane.showMessageDialog(null, message, "About", JOptionPane.INFORMATION_MESSAGE);
        }


        private void initializeButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(rows, cols));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 14));
                buttons[i][j].setMargin(new Insets(1, 1, 1, 1));
                buttons[i][j].setBackground(Color.LIGHT_GRAY);
                buttons[i][j].setHorizontalAlignment(SwingConstants.CENTER);
                buttons[i][j].setVerticalAlignment(SwingConstants.CENTER);
                buttons[i][j].setText("");
                panel.add(buttons[i][j]);
                buttons[i][j].addActionListener(new ButtonListener(i, j));
                buttons[i][j].addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            JButton button = (JButton)e.getSource();
                            if (button.isEnabled()) {
                                if (button.getIcon() == flagIcon) {
                                    button.setIcon(null);
                                } else {
                                    button.setIcon(flagIcon);
                                }
                            }
                        }
                    }
                });
            }
        }

        add(panel, BorderLayout.CENTER);
    }

    private void initializeTimer() {
        timerLabel = new JLabel("Time: 0");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(timerLabel, BorderLayout.SOUTH);

        timer = new Timer(1000, e -> {
            elapsedTime++;
            timerLabel.setText("Time: " + elapsedTime);
        });
        timer.start();
    }

    private void placeMines(int mineCount) {
        mines = new int[rows][cols];
        int count = 0;
        while (count < mineCount) {
            int x = (int)(Math.random() * rows);
            int y = (int)(Math.random() * cols);
            if (mines[x][y] != -1) {
                mines[x][y] = -1;
                count++;
            }
        }
    }

    private void calculateAdjacentNumbers() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mines[i][j] != -1) {
                    int count = 0;
                    for (int x = i - 1; x <= i + 1; x++) {
                        for (int y = j - 1; y <= j + 1; y++) {
                            if (x >= 0 && x < rows && y >= 0 && y < cols) {
                                if (mines[x][y] == -1) {
                                    count++;
                                }
                            }
                        }
                    }
                    mines[i][j] = count;
                }
            }
        }
    }

    private Color getColorForNumber(int number) {
        return switch (number) {
            case 1 -> Color.BLUE;
            case 2 -> new Color(0, 128, 0); // Dark Green
            case 3 -> Color.RED;
            case 4 -> new Color(128, 0, 128); // Purple
            case 5 -> new Color(128, 0, 0); // Maroon
            case 6 -> Color.CYAN;
            case 7 -> Color.BLACK;
            case 8 -> Color.GRAY;
            default -> Color.black;
        };
    }

    private void revealMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mines[i][j] == -1) {
                    buttons[i][j].setIcon(mineIcon);
                    buttons[i][j].setDisabledIcon(mineIcon);
                    buttons[i][j].setEnabled(false);
                }
            }
        }
    }

    private void revealAdjacentZeros(int x, int y) {
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i >= 0 && i < rows && j >= 0 && j < cols) {
                    if (buttons[i][j].isEnabled() && buttons[i][j].getIcon() != flagIcon) {
                        buttons[i][j].setEnabled(false);
                        int value = mines[i][j];
                        if (value == 0) {
                            buttons[i][j].setText("");
                            revealAdjacentZeros(i, j);
                        } else if (value > 0) {
                            buttons[i][j].setText(String.valueOf(value));
                            buttons[i][j].setForeground(getColorForNumber(value));
                        }
                    }
                }
            }
        }
    }

    private void disableAllButtons() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private boolean checkWin() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mines[i][j] != -1 && buttons[i][j].isEnabled()) {
                    return false;
                }
            }
        }
        return true;
    }

    private class ButtonListener implements ActionListener {
        private final int x, y;

        public ButtonListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void actionPerformed(ActionEvent e) {
            if (gameOver) {
                return;
            }

            JButton button = buttons[x][y];

            if (button.getIcon() == flagIcon) {
                return;
            }

            int value = mines[x][y];

            button.setEnabled(false);

            if (value == -1) {
                button.setIcon(mineIcon);
                button.setDisabledIcon(mineIcon);
                revealMines();
                JOptionPane.showMessageDialog(null, "You hit a mine! You lost the game.");
                disableAllButtons();
                gameOver = true;
                timer.stop();
            } else if (value == 0) {
                button.setText("");
                revealAdjacentZeros(x, y);
            } else {
                button.setText(String.valueOf(value));
                button.setForeground(getColorForNumber(value));
            }

            if (checkWin() && !gameOver) {
                revealMines();
                JOptionPane.showMessageDialog(null, "Congratulations! You found all the mines.");
                disableAllButtons();
                gameOver = true;
                timer.stop();
            }
        }
    }

    public static void main(String[] args) throws Exception {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        new Minesweeper(10, 10, 10);
    }
}
