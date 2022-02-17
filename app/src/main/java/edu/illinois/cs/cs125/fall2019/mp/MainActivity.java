package edu.illinois.cs.cs125.fall2019.mp;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents the main screen of the app, where the user will be able to view invitations and enter games.
 */
public final class MainActivity extends AppCompatActivity {

    /** UI container for invitations. */
    private LinearLayout invitationGroup;

    /** UI container for ongoing games. */
    private LinearLayout ongoingGamesGroup;

    /**
     * Called by the Android system when the activity is created.
     * @param savedInstanceState saved state from the previously terminated instance of this activity (unused)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // This "super" call is required for all activities
        super.onCreate(savedInstanceState);
        // Create the UI from a layout resource
        setContentView(R.layout.activity_main);



        // This activity doesn't do anything yet - it immediately launches the game activity
        // Work on it will start in Checkpoint 1

        // Intents are Android's way of specifying what to do/launch
        // Here we create an Intent for launching GameActivity and act on it with startActivity
//        startActivity(new Intent(this, GameActivity.class));
        // End this activity so that it's removed from the history
//        finish();
        // Otherwise pressing the back button in the game would come back to a blank screen here
        invitationGroup = findViewById(R.id.invitationsGroup);
        ongoingGamesGroup = findViewById(R.id.ongoingGamesGroup);
        invitationGroup.setVisibility(View.GONE);
        ongoingGamesGroup.setVisibility(View.GONE);
        connect();
        Intent createGameIntent = new Intent(this, NewGameActivity.class);
        Button createGame = findViewById(R.id.createGame);
        createGame.setOnClickListener(v -> {
            startActivity(createGameIntent);
            finish();
        });
    }

    // The functions below are stubs that will be filled out in Checkpoint 2

    /**
     * Starts an attempt to connect to the server to fetch/refresh games.
     */
    private void connect() {
        // Make any "loading" UI adjustments you like
        // Use WebApi.startRequest to fetch the games lists
        // In the response callback, call setUpUi with the received data
        System.out.println("connect();");
        WebApi.startRequest(this, WebApi.API_BASE + "/games", response -> {
            // Code in this handler will run when the request completes successfully
            // Do something with the response?
            System.out.println("--setUpUi--");
            setUpUi(response);
        }, error -> {
                Toast.makeText(this, "Oh no! Couldn't retrieve ongoing Games",
                    Toast.LENGTH_LONG).show();
                System.out.println("Error");
            });
    }

    /**
     * Populates the games lists UI with data retrieved from the server.
     * @param result parsed JSON from the server
     */
    private void setUpUi(final JsonObject result) {
        JsonArray gamesList = result.get("games").getAsJsonArray();
        ((LinearLayout) findViewById(R.id.invitationsList)).removeAllViews();
        ((LinearLayout) findViewById(R.id.ongoingGamesList)).removeAllViews();
        String[] teamNames = getResources().getStringArray(R.array.team_choices);

        //look through each game
        for (JsonElement game : gamesList) {
            String gameID = game.getAsJsonObject().get("id").getAsString();
            String ownerEmail = game.getAsJsonObject().get("owner").getAsString();
            JsonArray playersList = game.getAsJsonObject().get("players").getAsJsonArray();
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();


            if (game.getAsJsonObject().get("state").getAsInt() != GameStateID.ENDED) {
                for (JsonElement player : playersList) {
                    //if the user is in the game
                    String playerEmail = player.getAsJsonObject().get("email").getAsString();
                    if (playerEmail.equals(userEmail)) {
                        //check game state
                        // if (game.getAsJsonObject().get("state").getAsInt() != GameStateID.ENDED) {
                        int playerState = player.getAsJsonObject().get("state").getAsInt();
                        View chunk;
                        LinearLayout list;
                        //check player state
                        if (playerState == PlayerStateID.INVITED) {
                            invitationGroup.setVisibility(View.VISIBLE);
                            list = findViewById(R.id.invitationsList);
                            System.out.println("player is invited");
                            chunk = getLayoutInflater().inflate(R.layout.chunk_invitations,
                                    list, false);
                            Button accept = chunk.findViewById(R.id.acceptInvite);
                            accept.setOnClickListener(v -> {
                                gameButtonAction(gameID, "accept");
                            });
                            Button decline = chunk.findViewById(R.id.declineInvite);
                            decline.setOnClickListener(v -> {
                                gameButtonAction(gameID, "decline");
                            });

                        } else {
                            ongoingGamesGroup.setVisibility(View.VISIBLE);
                            list = findViewById(R.id.ongoingGamesList);
                            chunk = getLayoutInflater().inflate(R.layout.chunk_ongoing_game,
                                    list, false);
                            System.out.println("player is in game");
                            Button enter = chunk.findViewById(R.id.enterGame);
                            enter.setOnClickListener(v -> {
                                enterGame(gameID);
                            });
                            Button leave = chunk.findViewById(R.id.leaveGame);
                            if (ownerEmail.equals(userEmail)) {
                                leave.setVisibility(View.GONE);
                            }
                            leave.setOnClickListener(v -> {
                                gameButtonAction(gameID, "leave");
                            });
                        }

                        //set team
                        TextView team = chunk.findViewById(R.id.team);
                        int teamId = player.getAsJsonObject().get("team").getAsInt();
                        team.setText(teamNames[teamId]);
                        //set owner
                        TextView owner = chunk.findViewById(R.id.owner);
                        owner.setText(ownerEmail);
                        //set mode
                        TextView mode = chunk.findViewById(R.id.mode);
                        String gameMode = game.getAsJsonObject().get("mode").getAsString();
                        mode.setText(gameMode + " mode");
                        //System.out.println(mode.getText());
                        //add chunk to the LinearLayout
                        list.addView(chunk);
                        // }
                    }
                }
            }
            //look through each player in the game

        }



        // Hide any optional "loading" UI you added
        // Clear the games lists
        // Add UI chunks to the lists based on the result data
    }


    /** makes a POST request for the given game.
     * @param gameId id of the game
     * @param action the type of request
     */
    private void gameButtonAction(final String gameId, final String action) {
        System.out.println("try to " + action);
        WebApi.startRequest(this, WebApi.API_BASE + "/games/" + gameId + "/" + action,
                Request.Method.POST, null, response -> {
                System.out.println(action + " complete");
                connect();
            }, error -> {
                Toast.makeText(this, "Oops something went wrong!",
                        Toast.LENGTH_LONG).show();
                System.out.println("404 cannot" + action);
            });
    }
    /**
     * Enters a game (shows the map).
     * @param gameId the ID of the game to enter
     */
    private void enterGame(final String gameId) {
        Intent game = new Intent(this, GameActivity.class);
        game.putExtra("game", gameId);
        startActivity(game);
    }

}
