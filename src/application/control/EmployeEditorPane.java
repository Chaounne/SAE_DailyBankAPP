package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.EmployeEditorPaneController;
import application.view.EmployeManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Employe;

public class EmployeEditorPane {

	private Stage primaryStage;
	private EmployeEditorPaneController eepc;

	/**
	 * Initialisation de la fenêtre des employés
	 * @param _parentStage
	 * @param _dbstate
	 */
	public EmployeEditorPane(Stage _parentStage, DailyBankState _dbstate) {
		
		try {
			FXMLLoader loader = new FXMLLoader(EmployeManagementController.class.getResource("employeeditorpane.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+20, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion d'un employé");
			this.primaryStage.setResizable(false);

			this.eepc = loader.getController();
			this.eepc.initContext(this.primaryStage, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *Rend disponible les options de gestion d'un employé selon les droits de l'utilisateur et dans quel état on est (suppression,modification ou creation)
	 * @param employe
	 * @param em
	 * @return this.cepc.displayDialog(employe,em)
	 */
	public Employe doEmployeEditorDialog(Employe employe, EditionMode em) {
		return this.eepc.displayDialog(employe, em);
	}
	
}
