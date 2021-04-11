import java.util.Random;
import java.util.Scanner;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.sql.*;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;

public class Snake extends Application {

    private Random r = new Random();
    private int counter = 3;
    private Rectangle[] rect = new Rectangle[100];
    private Rectangle food;
    private int[] xc = new int[100];
    private int[] yc = new int[100];
    private int dir = 3;
    private Pane root = new Pane();
    private boolean isDone = false;
    private int grow = 0;
    private int[] foodloc, headloc;
    private int score = 0;
    private String name = "Default Player";



    private Rectangle initRect() {
        Rectangle res = new Rectangle(45, 45);
        res.setFill(Color.BURLYWOOD);
        res.setStroke(Color.BLACK);
        res.setVisible(false);
        return res;
    }

    // creating table for database
    private void createTable() {
      Connection c = null;
      Statement stmt = null;
      
      try {
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:test.db");
         System.out.println("Opened database successfully");

         stmt = c.createStatement();
         String sql = "CREATE TABLE IF NOT EXISTS HIGH_SCORE " +
                        "(TIME TEXT," +
                        " NAME TEXT    , " + 
                        " SCORE  INT NOT NULL)"; 
         stmt.executeUpdate(sql);
         stmt.close();
         c.close();
      } catch ( Exception e ) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         System.exit(0);
      }
      System.out.println("Table created successfully\n");

      Scanner sc = new Scanner(System.in);
      System.out.print("Enter Player Name: ");
      this.name = sc.nextLine();
    }

    //inserting score in table
    private void insertScore(String arg_name, String arg_score, String arg_time) {
        Connection c = null;
      Statement stmt = null;
      
      try {
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:test.db");
         c.setAutoCommit(false);
         System.out.println("Inserting Score into database");

         stmt = c.createStatement();
         String sql = "INSERT INTO HIGH_SCORE (NAME,SCORE,TIME) " +
                        "VALUES ('"+arg_name+"', "+arg_score+", '"+arg_time+"');"; 

         stmt.executeUpdate(sql);

         stmt.close();
         c.commit();
         c.close();
      } catch ( Exception e ) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         System.exit(0);
      }
      System.out.println("Records created successfully");
    }

    // showing scores
    private void showScore() {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            c.setAutoCommit(false);
            System.out.println("\nShowing All Scores (High Score to Low Score):");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM HIGH_SCORE ORDER BY SCORE DESC;" );
            
            int p_pos = 0;
            while ( rs.next() ) {
                p_pos++;
                String  p_name = rs.getString("name");
                int p_score  = rs.getInt("score");
                String  p_time = rs.getString("time");
                
                System.out.print( "Player No: " + p_pos+" - ");
                System.out.print( "NAME = " + p_name +" , ");
                System.out.print( "Score = " + p_score + " , ");
                System.out.print( "TIME = " + p_time );
                
                System.out.println();
            }
            rs.close();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }

    public void start(Stage primaryStage) {

        createTable();

        headloc = new int[]{xc[0], yc[0]};
        Thread game;
        System.out.println(xc[0] + " " + xc[1] + " " + xc[2] + " ");
        food = initRect();
        food.setStroke(Color.RED);
        food.setVisible(true);
        food.setTranslateX(r.nextInt(10) * 50);
        food.setTranslateY(r.nextInt(10) * 50);
        root.getChildren().add(food); // Adding food
        
        Scene scene = new Scene(root, 514, 543);
        for (int i = 0; i < 3; i++) {
            rect[i] = initRect();
            rect[i].setTranslateX(50 + 50 * i);
            rect[i].setTranslateY(50 + 50);
            rect[i].setVisible(true);
            root.getChildren().add(rect[i]);
        }
        for (int i = 3; i < 100; i++) {
            rect[i] = initRect();
            rect[i].setTranslateX(50 + 50 * i);
            rect[i].setTranslateY(50 + 50);
            root.getChildren().add(rect[i]);
        }
        rect[0].setFill(Color.RED);
        game = new Thread(() -> {
            while (!isDone) {
                move();
                try {
                    Thread.sleep(200);
                } catch (Exception ex) {
                }
            }
        });
        game.start();
        scene.setOnKeyPressed(event -> {
            KeyCode k = event.getCode();
            switch (k) {
                case D:
                    if (dir != 1 && ((dir == 2 || dir == 0) && headloc[1] != yc[0])) dir = 3;
                    break;
                case A:
                    if (dir != 3 && ((dir == 2 || dir == 0) && headloc[1] != yc[0])) dir = 1;
                    break;
                case S:
                    if (dir != 2 && ((dir == 3 || dir == 1) && headloc[0] != xc[0])) dir = 0;
                    break;
                case W:
                    if (dir != 0 && ((dir == 3 || dir == 1) && headloc[0] != xc[0])) dir = 2;
                    break;
            }
            headloc = new int[]{xc[0], yc[0]};
        });
        primaryStage.setTitle("Snake");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(543);
        primaryStage.setMinWidth(514);
        primaryStage.setMaxHeight(543);
        primaryStage.setMaxWidth(514);
        primaryStage.setOnCloseRequest(event -> isDone = true);
        primaryStage.show();

    }

    private void move() {
        int[][] tmp = {{xc[1], yc[1]}, {}};
        xc[1] = xc[0];
        yc[1] = yc[0];
        for (int i = 2; i < counter; i++) {
            tmp[1] = new int[]{xc[i], yc[i]};
            xc[i] = tmp[0][0];
            yc[i] = tmp[0][1];
            tmp[0] = tmp[1];
            if (grow > 0 && xc[counter - 1] == foodloc[0] && yc[counter - 1] == foodloc[1]) {
                rect[counter - 1].setVisible(true);
                --grow;
            }
        }
        switch (dir) {
            case 0:
                if (yc[0] + 50 <= 450) yc[0] += 50;
                else yc[0] = 0;
                break;
            case 1:
                if (xc[0] - 50 >= 0) xc[0] -= 50;
                else xc[0] = 450;
                break;
            case 2:
                if (yc[0] - 50 >= 0) yc[0] -= 50;
                else yc[0] = 450;
                break;
            case 3:
                if (xc[0] + 50 <= 450) xc[0] += 50;
                else xc[0] = 0;
                break;
        }
        if (intersects(xc[0], yc[0])) { // Now The game is over
            System.out.println("GAME OVER");
            System.out.println("\nGame Results : - \n\nYOUR SCORE: "+score); // Printing Your Score
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
            LocalDateTime now = LocalDateTime.now();  
            
            System.out.println("");
            insertScore(this.name,String.valueOf(this.score),dtf.format(now));
            System.out.println("");
            showScore();
            System.out.println("");
            isDone = true;
        } else {
            for (int i = 0; i < counter; i++) {
                rect[i].setTranslateX(xc[i]);
                rect[i].setTranslateY(yc[i]);
            }
            if (rect[0].getBoundsInParent().intersects(food.getBoundsInParent())) {
                boolean isChanged = false;
                foodloc = new int[]{(int) food.getTranslateX(), (int) food.getTranslateY()};
                while (!isChanged) {
                    food.setTranslateX(r.nextInt(10) * 50);
                    food.setTranslateY(r.nextInt(10) * 50);
                    if (intersects(foodloc[0], foodloc[1]) && (int) food.getTranslateX() != foodloc[0] || (int) food.getTranslateY() != foodloc[1])
                        isChanged = true;
                }
                xc[counter] = (int) food.getTranslateX();
                yc[counter] = (int) food.getTranslateY();
                rect[counter].setTranslateX(rect[counter - 1].getX());
                rect[counter].setTranslateY(rect[counter - 1].getY());
                ++counter;
                ++grow;
                ++score;
                System.out.println(counter + "");
            }
        }
    }

    public boolean intersects(int x, int y) {
        int i = 0;
        for (Rectangle part : rect) {
            if (part != rect[0] && i > 0 && part.isVisible() && x == xc[i] && y == yc[i]) {
                System.out.println(i);
                return true;
            }
            i++;
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }

}