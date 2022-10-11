package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.CompteEditorPaneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;

/**
 * @author Enzo
 * Pane de la page d'édition des comptes bancaires
 */
public class CompteEditorPane {

	private Stage primaryStage;
	private CompteEditorPaneController cepc;

	/**
	 * Fenêtre d'édition des comptes 
	 * @param _parentStage
	 * @param _dbstate
	 */
	public CompteEditorPane(Stage _parentStage, DailyBankState _dbstate) {

		try {
			FXMLLoader loader = new FXMLLoader(CompteEditorPaneController.class.getResource("compteeditorpane.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+20, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion d'un compte");
			this.primaryStage.setResizable(false);

			this.cepc = loader.getController();
			this.cepc.initContext(this.primaryStage, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rend disponible les options de gestion d'un compte selon les droits de l'utilisateur et dans quel état on est (suppression,modification ou creation)
	 * @param client
	 * @param cpte
	 * @param em
	 * @return this.cepc.displayDialog(client, cpte, em)
	 */
	public CompteCourant doCompteEditorDialog(Client client, CompteCourant cpte, EditionMode em) {
		return this.cepc.displayDialog(client, cpte, em);
	}
}
