package application.control;

import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.ComptesManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;
import model.orm.AccessCompteCourant;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.Table;

/**
 * @author Enzo
 * Code de la fenêtre d'affichage des comptes menant à la fenêtre de modification des comptes
 */
public class ComptesManagement {

	private Stage primaryStage;
	private ComptesManagementController cmc;
	private DailyBankState dbs;
	private Client clientDesComptes;

	/**
	 * Fenêtre de gestion des comptes d'un client
	 * @param _parentStage
	 * @param _dbstate
	 * @param client
	 */
	public ComptesManagement(Stage _parentStage, DailyBankState _dbstate, Client client) {

		this.clientDesComptes = client;
		this.dbs = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(ComptesManagementController.class.getResource("comptesmanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+50, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des comptes");
			this.primaryStage.setResizable(false);

			this.cmc = loader.getController();
			this.cmc.initContext(this.primaryStage, this, _dbstate, client);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rend disponible les options de gestion d'un compte selon les droits de l'utilisateur et dans quel état on est (suppression,modification ou creation)
	 */
	public void doComptesManagementDialog() {
		this.cmc.displayDialog();
	}

	/**
	 * Fenêtre de gestion des opérations d'un compte
	 * @param cpt
	 */
	public void gererOperations(CompteCourant cpt) {
		OperationsManagement om = new OperationsManagement(this.primaryStage, this.dbs, this.clientDesComptes, cpt);
		om.doOperationsManagementDialog();
		
		
	}
	public void gererPrelevement(CompteCourant cpt) {
		PrelevementManagement pm = new PrelevementManagement(this.primaryStage, this.dbs, this.clientDesComptes, cpt);
		pm.doPrelevementManagementDialog();
	}

	/**
	 * Fenêtre de création d'un nouveau compte pour un client 
	 * @return le nouveau compte (compte)
	 */
	public CompteCourant creerCompte() {
		CompteCourant compte;
		CompteEditorPane cep = new CompteEditorPane(this.primaryStage, this.dbs);
		compte = cep.doCompteEditorDialog(this.clientDesComptes, null, EditionMode.CREATION);
		if (compte != null) {
			try {
				AccessCompteCourant acc = new AccessCompteCourant();
				
				acc.creerCompteCourant(compte);
				
				// if JAMAIS vrai
				// existe pour compiler les catchs dessous
				if (Math.random() < -1) {
					throw new ApplicationException(Table.CompteCourant, Order.INSERT, "todo : test exceptions", null);
				}
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
			}
		}
		return compte;
	}

	/**
	 * Affiche la liste des comptes d'un client
	 * @return la liste (listeCpt)
	 */
	public ArrayList<CompteCourant> getComptesDunClient() {
		ArrayList<CompteCourant> listeCpt = new ArrayList<>();

		try {
			AccessCompteCourant acc = new AccessCompteCourant();
			listeCpt = acc.getCompteCourants(this.clientDesComptes.idNumCli);
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeCpt = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			listeCpt = new ArrayList<>();
		}
		return listeCpt;
	}
	
	/**
	 * Cloturer (fermer) un compte d'un client
	 * @param compte
	 */
	public void cloturerCompte(CompteCourant compte) throws DatabaseConnexionException, ApplicationException {
		AccessCompteCourant.cloturer(compte);
		
	}
	public void realiserSimulation() {
		Simulation sm = new Simulation(this.primaryStage);
		sm.doSimulationDialog();
	}
}
