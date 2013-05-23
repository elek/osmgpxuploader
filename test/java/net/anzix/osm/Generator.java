package net.anzix.osm;

/**
 * User: eszti
 */
public class Generator {
    public static void main(String[] args) {
        Schema schema = new Schema(1, "de.greenrobot.daoexample");

        Entity note= schema.addEntity("Note");
        note.addIdProperty();
        note.addStringProperty("text").notNull();
        note.addStringProperty("comment");
        note.addDateProperty("date");

        new DaoGenerator().generateAll("../DaoExample/src-gen", schema);
    }
}
