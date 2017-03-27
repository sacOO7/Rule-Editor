package sample;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.json.JSONObject;

/**
 * Created by sachin on 24/3/17.
 */
public class RuleModel extends RecursiveTreeObject<RuleModel> {
    StringProperty name;
    StringProperty description;
    Boolean Recommended;
    StringProperty value;
    Boolean Fixable;
    StringProperty link;



    RuleModel(JSONObject object){
        name=new SimpleStringProperty(object.optString("Name"));
        description=new SimpleStringProperty(object.optString("Desc"));
        Fixable=object.optBoolean("Fixable");
        Recommended=object.optBoolean("Recommended");
        link=new SimpleStringProperty(object.optString("Link"));
        if (object.opt("Value")!=null){
            value=new SimpleStringProperty(object.opt("Value").toString());
        }
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public Boolean getRecommended() {
        return Recommended;
    }

    public void setRecommended(Boolean recommended) {
        Recommended = recommended;
    }

    public Boolean getFixable() {
        return Fixable;
    }

    public void setFixable(Boolean fixable) {
        Fixable = fixable;
    }

    public String getLink() {
        return link.get();
    }

    public StringProperty linkProperty() {
        return link;
    }

    public void setLink(String link) {
        this.link.set(link);
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }
}
