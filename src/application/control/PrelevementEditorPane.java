package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.PrelevementEditorPaneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.CompteCourant;
import model.data.PrelevementAutomatique;

/**
 * @author Enzo
 * Pane de la page d'édition des comptes bancaires
 */
public class PrelevementEditorPane {

	private Stage primaryStage;
	private PrelevementEditorPaneController pepc;

	/**
	 * Fenètre d'édition des comptes 
	 * @param _parentStage
	 * @param _dbstate
	 */
	public PrelevementEditorPane(Stage _parentStage, DailyBankState _dbstate) {

		try {
			FXMLLoader loader = new FXMLLoader(PrelevementEditorPaneController.class.getResource("prelevementeditorpane.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+20, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion d'un prélèvement automatique");
			this.primaryStage.setResizable(false);

			this.pepc = loader.getController();
			this.pepc.initContext(this.primaryStage, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rend disponible les options de gestion des prélèvements d'un compte selon les droits de l'utilisateur et dans quel état on est (suppression,modification ou creation)
	 * @param client
	 * @param cpte
	 * @param em
	 * @return this.cepc.displayDialog(client, cpte, em)
	 */
	public PrelevementAutomatique doPrelevementEditorDialog(CompteCourant compte, PrelevementAutomatique prlv, EditionMode em) {
		return this.pepc.displayDialog(compte, prlv, em);
	}
}
