package fr.iutlens.roguezombie;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import fr.iutlens.roguezombie.maze.Maze;
import fr.iutlens.roguezombie.maze.MiniMapView;
import fr.iutlens.roguezombie.room.OnRoomOutListener;
import fr.iutlens.roguezombie.room.RoomView;
import fr.iutlens.roguezombie.joystick.JoystickView;
import fr.iutlens.roguezombie.room.sprite.HeroSprite;
import fr.iutlens.roguezombie.room.sprite.Sprite;
import fr.iutlens.roguezombie.util.Coordinate;


public class MainActivity extends ActionBarActivity implements OnRoomOutListener {
    MediaPlayer ourSong;

    private Maze maze;
    private Coordinate coordinate;
    private RoomView roomView;
    private MiniMapView miniMapView;
    private int score;
    private JoystickView joystickView;
    private long countDown;
    private boolean running;
    private boolean fin=false;

    @Override
    public void onRoomOut(int x, int y, int dir) {
        //Calcul de la prochaine salle dans la direction indiquée
        int ndx = coordinate.getNext(coordinate.getNdx(x, y), dir);

       dir = maze.caseVisite(x, y);


        //mise à jour de la minimap
        maze.visit(ndx);
        miniMapView.invalidate();



        //mise à jour de la salle affichée
        roomView.setRoom(coordinate.getI(ndx), coordinate.getJ(ndx), dir);
    }


    //Gestion du temps réel
    static class RefreshHandler extends Handler {
        WeakReference<MainActivity> weak;

        RefreshHandler(MainActivity animator) {
            weak = new WeakReference(animator);
        }

        @Override
        public void handleMessage(Message msg) {
            if (weak.get() == null) return;
            weak.get().update();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    private RefreshHandler handler = new RefreshHandler(this);

    private void update() {
        countDown = countDown - 40;

        // test si fin (pedu : temps
        if (countDown < 0) {
                 popup("Vous avez echoué");
                 }

        if ( !fin ) { //on joue -- si pas fin
            handler.sleep(40);

            int longueur = (int) Math.round(joystickView.getRadial());

            roomView.move((float) joystickView.getAngle(), longueur == 0);
            roomView.act();

            if (roomView.hero.gagne) {
                popup("Vous avez gagné");
            }

            updateCountDown();
            updateScore();
            updateVie();
            if (roomView.hero.vie<1) {
                popup("Vous avez echoué");
            }
        } 


    }

    private void popup(String title) {
          // fin <- vrai
       fin = true;
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage("Vous avez mangé " + roomView.hero.score + " cerveaux, voulez vous recommencer ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ourSong.release();
                        startGame();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ourSong.release();
                        Intent intent = new Intent(MainActivity.this, Accueil.class);
                        startActivity(intent);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Création du layrinthe 10x10
        coordinate = new Coordinate(6, 6);

        miniMapView = (MiniMapView) findViewById(R.id.view);
        roomView = (RoomView) findViewById(R.id.view2);
        roomView.setListener(this);
        joystickView = (JoystickView) findViewById(R.id.joystick);
        startGame();
    }

    private void startGame() {
        maze = new Maze(coordinate);
        // Configuration de la minimap
        miniMapView.setMaze(maze);
        maze.visit(coordinate.getNdx(3, 3)); // On commence en 5x5
        // Configuration de la salle
        roomView.setMaze(maze, new Coordinate(10, 10));
        roomView.setRoom(3, 3, -1);
        countDown = 60 * 5 * 1000; // ICI TU MODIFIE LE TEMPS
        fin = false;
        // On démarre le jeu !
        ourSong = MediaPlayer.create(MainActivity.this, R.raw.music);
        ourSong.start();
        update();
    }

    // public void onButtonClick(View view){
    // Création du score ---------------------------------------------------------------------- MADE BY #TeamCoupDeGriffe --------------------------------------
    void updateScore() {
        ((TextView) findViewById(R.id.scoreView)).setText("Cerveaux : " + roomView.hero.score);
    }
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------

    //Création point de vie----------------------------------------------------------------------MADE BY #TeamCoupDeGriffe-----------------------------------------
    void updateVie() {
        ((TextView) findViewById(R.id.vieView)).setText("Vies : " + roomView.hero.vie);
       
    }
    //--------------------------------------------------------------------------------------------------------------------------------------------------------------

    //Création Timer----------------------------------------------------------------------MADE BY #TeamMoche-----------------------------------------
    void updateCountDown() {
        long minutes = countDown / 1000 / 60;
        int secondes = (int) (countDown / 1000 % 60);
        int sd = secondes/10;
        int su = secondes%10;
        ((TextView) findViewById(R.id.countDown)).setText("Temps : " + minutes + ":" + sd + su);
    }
    //--------------------------------------------------------------------------------------------------------------------------------------------------------------

//    public void onButtonClick(View view){

    // Détection de la direction choisie
    // int dir = -1;
    // switch (view.getId()){
    //   case R.id.buttonRight :
    // dir = 0;
    //  break;
    //case R.id.buttonDown :
    //    dir = 1;
    //  break;
    //case R.id.buttonLeft :
    //   dir = 2;
    //     break;
    //   case R.id.buttonUp :
    //        dir = 3;
    //         break;
    //   }

    // Demande le déplacement dans la direction
    //    roomView.move(dir);
    // }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


       /* int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
        switch (item.getItemId()) {

            case R.menu.menu_accueil:
                ourSong.release();
                Intent accueil = new Intent(this, Accueil.class);
                startActivity(accueil);
                return true;

            default:
                ourSong.release();
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onPause(){
        super.onPause();
        ourSong.release();
    }


}




