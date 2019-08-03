import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;

public class Song implements Serializable {

    private List<String> artists;
    private String name;

    public Song(List<String> artists, String name) {
        this.artists = artists;
        this.name = name;
    }

    public List<String> getArtists() {
        return artists;
    }

    public String getName() {
        return name;
    }

    public String getFirstArtist() {
        return artists.get(0);
    }

    public String getAllArtists() {
        StringJoiner sj = new StringJoiner(" ");
        artists.forEach(sj::add);
        return sj.toString();
    }

    public String getQueryString() {
        String cleanName = name.replaceAll("&", "and");
        String cleanArtist = artists.get(0).replaceAll("&", "and");

        return cleanName + " " + cleanArtist;
    }
}
