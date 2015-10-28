package GUI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Created by User on 27.10.2015.
 */
public class ErrorMessageStage {
    //список ошибок, произошедших во время работы
    private ArrayList<String> errorMesseges;
    private Stage MessageStage;

    public ErrorMessageStage(ArrayList<String> errorMessages, Stage parent){
        this.errorMesseges = errorMessages;
        MessageStage = new Stage();
        MessageStage.setTitle("Error");
        MessageStage.initOwner(parent);
        MessageStage.setAlwaysOnTop(true);
        MessageStage.initModality(Modality.WINDOW_MODAL);
        this.showMessage();
    }

    private void showMessage () {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        int cur;
        for (cur = 0; cur<errorMesseges.size(); cur++)
        {
            Label error = new Label();
            error.setText(this.errorMesseges.get(cur));
            grid.add(error, 0, cur);
        }
        Button ok = new Button("OK");
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                MessageStage.close();
            }
        });
        grid.add(ok, 0, cur+1);
        MessageStage.setScene(new Scene(grid, 300, 300));
        MessageStage.show();
    }
}