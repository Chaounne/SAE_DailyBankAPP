package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.ClientEditorPaneController;
import application.view.ClientsManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;

/**
 * @author Enzo 
 * Pane de la fenêtre d'édition des clients
 */
public class ClientEditorPane {

	private Stage primaryStage;
	private ClientEditorPaneController cepc;

	/**
	 * Initialisation de la fenêtre des clients
	 * @param _parentStage
	 * @param _dbstate
	 */
	public ClientEditorPane(Stage _parentStage, DailyBankState _dbstate) {

		try {
			FXMLLoader loader = new FXMLLoader(ClientsManagementController.class.getResource("clienteditorpane.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+20, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion d'un client");
			this.primaryStage.setResizable(false);

			this.cepc = loader.getController();
			this.cepc.initContext(this.primaryStage, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rend disponible les options de gestion d'un client celon les droits de l'utilisateur et dans quel état on est (suppression,modification ou creation)
	 * @param client
	 * @param em
	 * @return this.cepc.displayDialog(client,em)
	 */
	public Client doClientEditorDialog(Client client, EditionMode em) {
		return this.cepc.displayDialog(client, em);
	}
}
