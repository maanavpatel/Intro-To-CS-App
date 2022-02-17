package edu.illinois.cs.cs125.fall2019.mp;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/** Represents a target in an ongoing target-mode game and manages the marker displaying it.*/
public class Target {
    /** map. */
    private com.google.android.gms.maps.GoogleMap map;
    /** target position. */
    private com.google.android.gms.maps.model.LatLng position;
    /** team. */
    private int team;
    /** Marker options. */
    private MarkerOptions options;
    /** marker. */
    private Marker marker;

    /**
     * Creates a target in a target-mode game by placing an appropriately colored marker on the map.
     * The marker's hue should reflect the team that captured the target. See the class description
     * for the hue values to use.
     * @param setMap the map to render to
     * @param setPosition the position of the target
     * @param setTeam the TeamID code of the team currently owning the target
     */
    public Target(final com.google.android.gms.maps.GoogleMap setMap,
                  final com.google.android.gms.maps.model.LatLng setPosition,
                  final int setTeam) {
        map = setMap;
        position = setPosition;
        team = setTeam;
        options = new MarkerOptions().position(position);
        marker = map.addMarker(options);
        setTargetColor(team);
    }

    /** sets target color by team.
     * @param t team
     */
    private void setTargetColor(final int t) {
        float hue;
        switch (team) {
            case TeamID.TEAM_BLUE:
                hue = BitmapDescriptorFactory.HUE_BLUE;
                break;
            case TeamID.TEAM_GREEN:
                hue = BitmapDescriptorFactory.HUE_GREEN;
                break;
            case TeamID.TEAM_RED:
                hue = BitmapDescriptorFactory.HUE_RED;
                break;
            case TeamID.TEAM_YELLOW:
                hue = BitmapDescriptorFactory.HUE_YELLOW;
                break;
            default:
                hue = BitmapDescriptorFactory.HUE_VIOLET;
        }
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(hue);
        marker.setIcon(icon);
    }
    /** Gets the position of the target.
     * @return the coordinates of the target. */
    public com.google.android.gms.maps.model.LatLng getPosition() {
        return position;
    }

    /***
     * Gets the ID of the team currently owning this target.
     * @return the owning team ID or OBSERVER if unclaimed
     */
    public int getTeam() {
        return team;
    }

    /***
     * Updates the owning team of this target and changes the marker hue appropriately.
     * @param newTeam newTeam - the ID of the team that captured the target
     */
    public void setTeam(final int newTeam) {
        team = newTeam;
        setTargetColor(newTeam);
    }
}

