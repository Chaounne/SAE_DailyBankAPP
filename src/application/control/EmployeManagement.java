package application.control;

import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.EmployeManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.Employe;
import model.orm.AccessClient;
import model.orm.AccessEmploye;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

public class EmployeManagement {
	
	private Stage primaryStage;
	private DailyBankState dbs;
	private EmployeManagementController emc;
	
	
	public EmployeManagement(Stage _parentStage,DailyBankState _dbstate) {
		this.dbs = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(EmployeManagementController.class.getResource("employemanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+50, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des employés");
			this.primaryStage.setResizable(false);

			this.emc = loader.getController();
			this.emc.initContext(this.primaryStage, this, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<Employe> getlisteEmployes(int _numEmploye, String _debutNom, String _debutPrenom) {
		ArrayList<Employe> listeEmp = new ArrayList<>();
		try {
			// Recherche des clients en BD. cf. AccessClient > getClients(.)
			// numCompte != -1 => recherche sur numCompte
			// numCompte != -1 et debutNom non vide => recherche nom/prenom
			// numCompte != -1 et debutNom vide => recherche tous les clients

			AccessEmploye ae = new AccessEmploye();
			listeEmp = ae.getEmployes(this.dbs.getEmpAct().idAg, _numEmploye, _debutNom, _debutPrenom);

		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeEmp = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			listeEmp = new ArrayList<>();
		}
		return listeEmp;
	}
	
	/**
	 * Rend disponible les options de gestion d'un employe selon les droits de l'utilisateur et dans quel état on est (suppression,modification ou creation)
	 */
	public void doEmployeManagementDialog() {
		this.emc.displayDialog();
	}
	
	/**
	 * Fenêtre de modification des infos d'un employe sélectionner
	 * @param e (l'employe à modifier)
	 * @return l'employe avec les infos modifié (result)
	 */
	public Employe modifierEmploye(Employe e) {
		EmployeEditorPane eep = new EmployeEditorPane(this.primaryStage, this.dbs);
		Employe result = eep.doEmployeEditorDialog(e, EditionMode.MODIFICATION);
		if (result != null) {
			try {
				AccessEmploye ae = new AccessEmploye();
				ae.updateEmploye(result);
			} catch (DatabaseConnexionException ec) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ec);
				ed.doExceptionDialog();
				result = null;
				this.primaryStage.close();
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				result = null;
			}
		}
		return result;
	}
	
	
	/**
	 * Fenêtre de création d'un nouvel employe
	 * @return le nouvel employe
	 */
	public Employe nouvelEmploye() {
		Employe employe;
		EmployeEditorPane cep = new EmployeEditorPane(this.primaryStage, this.dbs);
		employe = cep.doEmployeEditorDialog(null, EditionMode.CREATION);
		if (employe != null) {
			try {
				AccessEmploye ae = new AccessEmploye();

				ae.insertEmploye(employe);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				employe = null;
			} catch (ApplicationException aet) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, aet);
				ed.doExceptionDialog();
				employe = null;
			}
		}
		return employe;
	}

	
	public void supprimerEmploye(Employe employe) {
		
		try {
			AccessEmploye ae = new AccessEmploye();
			
			ae.deleteEmploye(employe);
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
		} catch (ApplicationException aet) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, aet);
			ed.doExceptionDialog();
		}
	}
}
