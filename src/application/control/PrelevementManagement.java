package application.control;

import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.StageManagement;
import application.view.PrelevementManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;
import model.data.PrelevementAutomatique;
import model.orm.AccessPrelevement;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

public class PrelevementManagement {

	private Stage primaryStage;
	private DailyBankState dbs;
	private PrelevementManagementController omc;
	private Client clientDuCompte;
	private CompteCourant compteConcerne;

	public PrelevementManagement(Stage _parentStage, DailyBankState _dbstate, Client client, CompteCourant compte) {

		this.clientDuCompte = client;
		this.compteConcerne = compte;
		this.dbs = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(
					PrelevementManagementController.class.getResource("prelevementmanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, 900 + 20, 350 + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des opÃ©rations");
			this.primaryStage.setResizable(false);

			this.omc = loader.getController();
			this.omc.initContext(this.primaryStage, this, _dbstate, client, this.compteConcerne);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doPrelevementManagementDialog() {
		this.omc.displayDialog();
	}
	
	/**
	 * Affiche la liste des prélèvements automatiques d'un client
	 * @return la liste (listePrlv)
	 */
	public ArrayList<PrelevementAutomatique> getPrelevDunClient() {
		
		ArrayList<PrelevementAutomatique> listePrlv = new ArrayList<>();

		try {
			AccessPrelevement acp = new AccessPrelevement();
			listePrlv = acp.getPrelevements(this.clientDuCompte.idNumCli);
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listePrlv = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			listePrlv = new ArrayList<>();
		}
		return listePrlv;


	}
	}
