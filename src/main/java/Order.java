import java.util.ArrayList;
import java.util.List;

public class Order {
    private List<Hero> heroes = new ArrayList<>();

    public List<Hero> getHeroes() {
        if (heroes.isEmpty()) {
            BotLogger.info("No heroes added");
        }
        return heroes;
    }

    public void addHero(Hero hero) {
        if (heroes.contains(hero)) {
            BotLogger.warn("Hero already added: " + hero.getName());
            return;
        }
        heroes.add(hero);
    }
    public void removeHero(Hero hero){
        if (heroes.contains(hero)){
            heroes.remove(hero);
        }
        else{
            BotLogger.warn("Hero not removed (not found): " + hero.getName());
        }
    }

    public boolean hasRequiredCount(int expectedCount) {
        return heroes.size() >= expectedCount;
    }

    public String getInfo() {
        if (heroes.isEmpty()) {
            return "";
        }
        StringBuilder info = new StringBuilder();
        for (int i = 0; i < heroes.size(); i++) {
            if (i > 0) info.append(", ");
            info.append(heroes.get(i).getName());
        }
        return info.toString();
    }

    public boolean hasNoHeroes() {
        return heroes.isEmpty();
    }

    public boolean hasHero(String name) {
        for (Hero hero : heroes) {
            if (hero.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        heroes.clear();
    }
}
