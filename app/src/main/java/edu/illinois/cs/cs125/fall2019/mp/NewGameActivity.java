package edu.illinois.cs.cs125.fall2019.mp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * Represents the game creation screen, where the user configures a new game.
 */
public final class NewGameActivity extends AppCompatActivity {

    // This activity doesn't do much at first - it'll be worked on in Checkpoints 1 and 3

    /** The Google Maps view used to set the area for area mode. Null until getMapAsync finishes. */
    private GoogleMap areaMap;
    /** The Google Maps view used to set the area for area mode. Null until getMapAsync finishes. */
    private GoogleMap targetMap;
    /** The Radio Button Group from the game setup screen. */
    private RadioGroup gameModeGroup;
    /** The Radio Button for Target Mode. */
    private RadioButton targetMode;
    /** Linear layout of target mode setup. */
    private LinearLayout targetSettings;
    /** The Radio Button for Target Mode. */
    private RadioButton areaMode;
    /** Linear layout of area mode setup. */
    private LinearLayout areaSettings;
    /** RadioButton of the selected button. */
    private RadioButton selectedModeButton;
    /** List of Google Maps Markers representing added targets. */
    private List<Marker> markerList;
    /**invited players.*/
    private List<Invitee> invitees;
    /**
     * Called by the Android system when the activity is created.
     * @param savedInstanceState state from the previously terminated instance (unused)
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game); // app/src/main/res/layout/activity_new_game.xml
        setTitle(R.string.create_game); // Change the title in the top bar
        // Now that setContentView has been called, findViewById and findFragmentById work

        // Find the Google Maps component for the area map
        SupportMapFragment areaMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.areaSizeMap);
        // Start the process of getting a Google Maps object
        areaMapFragment.getMapAsync(newMap -> {
            // NONLINEAR CONTROL FLOW: Code in this block is called later, after onCreate ends
            // It's a "callback" - it will be called eventually when the map is ready

            // Set the map variable so it can be used by other functions
            areaMap = newMap;
            // Center it on campustown
            centerMap(areaMap);
        });

        markerList = new ArrayList<>();
        // Find the Google Maps component for the target map
        SupportMapFragment targetMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.targetsMap);
        // Start the process of getting a Google Maps object
        targetMapFragment.getMapAsync(newMap -> {
            // NONLINEAR CONTROL FLOW: Code in this block is called later, after onCreate ends
            // It's a "callback" - it will be called eventually when the map is ready

            // Set the map variable so it can be used by other functions
            targetMap = newMap;
            // Center it on campustown
            centerMap(targetMap);
            //create marker and add to list of all added targets
            targetMap.setOnMapLongClickListener(location -> {
                MarkerOptions options = new MarkerOptions().position(location);
                Marker marker = targetMap.addMarker(options);
                markerList.add(marker);
            });
            targetMap.setOnMarkerClickListener(selectedMarker -> {
                selectedMarker.remove();
                markerList.remove(selectedMarker);
                return true;
            });
        });

        /*
         * Setting an ID for a control in the UI designer produces a constant on R.id
         * that can be passed to findViewById to get a reference to that control.
         * Here we get a reference to the Create Game button.
         */
        Button createGame = findViewById(R.id.createGame);
        /*
         * Now that we have a reference to the control, we can use its setOnClickListener
         * method to set the handler to run when the user clicks the button. That function
         * takes an OnClickListener instance. OnClickListener, like many types in Android,
         * has exactly one function which must be filled out, so Java allows instances of it
         * to be written as "lambdas", which are like small functions that can be passed around.
         * The part before the arrow is the argument list (Java infers the types); the part
         * after is the statement to run. Here we don't care about the argument, but it must
         * be there for the signature to match.
         */
        createGame.setOnClickListener(unused -> createGameClicked());
        /*
         * It's also possible to make lambdas for functions that take zero or multiple parameters.
         * In those cases, the parameter list needs to be wrapped in parentheses, like () for a
         * zero-argument lambda or (someArg, anotherArg) for a two-argument lambda. Lambdas that
         * run multiple statements, like the one passed to getMapAsync above, look more like
         * normal functions in that they need their body wrapped in curly braces. Multi-statement
         * lambdas for functions with a non-void return type need return statements, again like
         * normal functions.
         */

        gameModeGroup = findViewById(R.id.gameModeGroup);
        targetMode = findViewById(R.id.targetModeOption);
        targetSettings = findViewById(R.id.targetSettings);
        areaMode = findViewById(R.id.areaModeOption);
        areaSettings = findViewById(R.id.areaSettings);
        gameModeGroup.setOnCheckedChangeListener((unused, checkedID) -> {

            if (targetMode.isChecked() && !areaMode.isChecked()) {
                targetSettings.setVisibility(View.VISIBLE);
                presetTargets();
            } else {
                targetSettings.setVisibility(View.GONE);
            }
            if (areaMode.isChecked() && !targetMode.isChecked()) {
                areaSettings.setVisibility(View.VISIBLE);
            } else {
                areaSettings.setVisibility(View.GONE);
            }
        });

        invitees = new ArrayList<>();
        invitees.add(new Invitee(FirebaseAuth.getInstance().getCurrentUser().getEmail(), TeamID.OBSERVER));
        updateInvitedPlayersUI();
        Button invite = findViewById(R.id.addInvitee);
        TextView inviteeEmail = findViewById(R.id.newInviteeEmail);
        invite.setOnClickListener(v -> {
            System.out.println();
            String email = inviteeEmail.getText().toString();
            if (!email.isEmpty()) {
                //add player to the list of invited peeps
                addInvitedPlayer(email);
            }
            //update the UI to reflect change
            updateInvitedPlayersUI();
            inviteeEmail.setText("");
        });
    }

    /**
     * Updates list with invited players and their roles.
     */
    private void updateInvitedPlayersUI() {
        LinearLayout invitedPlayers = findViewById(R.id.playersList);
        invitedPlayers.removeAllViews();
        for (Invitee i : invitees) {
            View chunk = getLayoutInflater().inflate(R.layout.chunk_invitee, invitedPlayers, false);
            //email text
            TextView email = chunk.findViewById(R.id.inviteeEmail);
            email.setText(i.getEmail());
            //team selection spinner
            Spinner team = chunk.findViewById(R.id.inviteeTeam);
            team.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> parent, final View view,
                                           final int position, final long id) {
                    i.setTeamId(position);
                }

                @Override
                public void onNothingSelected(final AdapterView<?> parent) {
                      //irrelevant for the MP
                }
            });
            team.setSelection(i.getTeamId());
            //remove button
            Button remove = chunk.findViewById(R.id.removeInvitee);
            if (i.getEmail() == FirebaseAuth.getInstance().getCurrentUser().getEmail()) {
                remove.setVisibility(View.GONE);
            }
            remove.setOnClickListener(v -> {
                invitees.remove(i);
                updateInvitedPlayersUI();
            });
            invitedPlayers.addView(chunk);
        }
    }

    /**
     * invites the player by adding them to the list when "Invite" button is pressed.
     * @param email player email to invite
     */
    private void addInvitedPlayer(final String email) {
        Invitee i = new Invitee(email, TeamID.OBSERVER);
        invitees.add(i);
    }

    /**
     * Sets up the area sizing map with initial settings: centering on campustown.
     * <p>
     * You don't need to alter or understand this function, but you will want to use it when
     * you add another map control in Checkpoint 3.
     * @param map the map to center
     */
    private void centerMap(final GoogleMap map) {
        // Bounds of campustown and some surroundings
        final double swLatitude = 40.098331;
        final double swLongitude = -88.246065;
        final double neLatitude = 40.116601;
        final double neLongitude = -88.213077;

        // Get the window dimensions (for the width)
        Point windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);

        // Convert 300dp (height of map control) to pixels
        final int mapHeightDp = 300;
        float heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mapHeightDp,
                getResources().getDisplayMetrics());

        // Submit the camera update
        final int paddingPx = 10;
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                new LatLng(swLatitude, swLongitude),
                new LatLng(neLatitude, neLongitude)), windowSize.x, (int) heightPx, paddingPx));
    }

    /**
     * Code to run when the Create Game button is clicked.
     */
    private void createGameClicked() {
        // Set up an Intent that will launch GameActivity
        Intent intent = new Intent(this, GameActivity.class);
      //  Intent newGame = new Intent(this, NewGameActivity.class);

        selectedModeButton = findViewById(gameModeGroup.getCheckedRadioButtonId());

//       this string will look at which radioButton is selected and return it's id */
        if (selectedModeButton != null) {
            String gameMode = selectedModeButton.getText().toString();
            intent.putExtra("mode", gameMode);
            JsonObject post = new JsonObject();
                        //JsonArray of invitees
            JsonArray inv = new JsonArray();
            for (Invitee i : invitees) {
                JsonObject playerInfo = new JsonObject();
                playerInfo.addProperty("email", i.getEmail());
                playerInfo.addProperty("team", i.getTeamId());
                inv.add(playerInfo);
            }
            //add to post Json
            post.add("invitees", inv);

            if (gameMode.equals(targetMode.getText().toString())) {
                //game mode;
                post.addProperty("mode", "target");

                EditText proxBox = findViewById(R.id.proximityThreshold);
                String proxText = proxBox.getText().toString();
                if (!proxText.equals("")) {
                    int proximityThreshold = Integer.parseInt(proxBox.getText().toString());
                    post.addProperty("proximityThreshold", proximityThreshold);
                }

                //for Target Mode add list of targets to Json
                //create Json array of target info (include latitude and longitude)
                JsonArray targets = new JsonArray();
                for (Marker m: markerList) {
                    JsonObject target = new JsonObject();
                    target.addProperty("latitude", m.getPosition().latitude);
                    target.addProperty("longitude", m.getPosition().longitude);
                    targets.add(target);
                }
                post.add("targets", targets);

            } else if (gameMode.equals(areaMode.getText().toString())) {
                post.addProperty("mode", "area");

                EditText cellSizeBox = findViewById(R.id.cellSize);
                String cellSizeText = cellSizeBox.getText().toString();
                if (!cellSizeText.equals("")) {
                    int cellSize = Integer.parseInt(cellSizeBox.getText().toString());
                    LatLngBounds boundaries = areaMap.getProjection().getVisibleRegion().latLngBounds;

                    post.addProperty("cellSize", cellSize);
                    post.addProperty("areaNorth", boundaries.northeast.latitude);
                    post.addProperty("areaEast", boundaries.northeast.longitude);
                    post.addProperty("areaSouth", boundaries.southwest.latitude);
                    post.addProperty("areaWest", boundaries.southwest.longitude);

                }
            }
            WebApi.startRequest(this, WebApi.API_BASE + "/games/create",
                    Request.Method.POST, post, response -> {
                //check gameId: if not null{ start game }
                    String gameId = response.get("game").getAsString();
                    if (gameId != null) {
                        intent.putExtra("game", gameId);
                        startActivity(intent);
                        finish();
                    }
                //error { show error message }
                }, error -> {
                    Toast.makeText(this, error.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
        }
    }

    /**Sets preset targets. */
    private void presetTargets() {
        Button loadPreset = findViewById(R.id.loadPresetTargets);
        loadPreset.setOnClickListener(v -> {
            //fetch preset from server done
            //get the targets and add to markerList
            //when request complete show alertdialog with preset options
            //use preset chunk to load options into the alertdialog
            WebApi.startRequest(this, WebApi.API_BASE + "/presets", response -> {
                // Code in this handler will run when the request completes successfully
                // Do something with the response?
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View presetChunk = getLayoutInflater().inflate(R.layout.chunk_presets_list,
                        null, false);

                RadioGroup presetOptions = presetChunk.findViewById(R.id.presetOptions);
                for (JsonElement e : response.get("presets").getAsJsonArray()) {
                    JsonObject o = e.getAsJsonObject();
                    String name = o.get("name").getAsString();
                    RadioButton b = new RadioButton(this);
                    b.setText(name);
                    //System.out.println(presetOptions.toString());
                    presetOptions.addView(b);
                }

                builder.setView(presetChunk)
                        .setPositiveButton("LOAD", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                //add targets to the marker list
                                //get name of selected option
                                String selected = ((Button) presetChunk.findViewById(presetOptions
                                        .getCheckedRadioButtonId())).getText().toString();
                                //selected not empty
                                //look through json elements, get json array for targets that is in the obj with same name as selected
                                // save that array to be accessed later
                                //remove markers
                                // add targets by looking through array
                                if (!selected.isEmpty()) {
                                    System.out.println(selected);
                                    JsonArray selectedPreset = null;
                                    for (JsonElement e : response.get("presets").getAsJsonArray()) {
                                        JsonObject o = e.getAsJsonObject();
                                        String name = o.get("name").getAsString();
                                        if (selected.equals(name)) {
                                            selectedPreset = o.get("targets").getAsJsonArray();
                                        }
                                    }
                                    //remove and clear previous targets
                                    for (Marker m : markerList) {
                                        m.remove();
                                    }
                                    markerList.clear();
                                    //add targets from selected preset
                                    for (JsonElement e : selectedPreset) {
                                        JsonObject t = e.getAsJsonObject();
                                        //
                                        LatLng position = new LatLng(t.get("latitude").getAsDouble(),
                                                t.get("longitude").getAsDouble());
                                        MarkerOptions options = new MarkerOptions().position(position);
                                        Marker marker = targetMap.addMarker(options);
                                        //
                                        markerList.add(marker);
                                    }
                                }
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }, error -> {
                    Toast.makeText(this, "Oh no! Couldn't retrieve ongoing Games",
                    Toast.LENGTH_LONG).show();
                    System.out.println("Error");
                });


        });
    }
}
