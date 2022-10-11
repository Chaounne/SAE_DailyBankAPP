package application.view;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.CategorieOperation;
import application.tools.ConstantesIHM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Operation;
import model.orm.*;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.Table;

public class OperationEditorPaneController implements Initializable {

	// Etat application
	private DailyBankState dbs;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private CategorieOperation categorieOperation;
	private CompteCourant compteEdite;
	private Operation operationResultat;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, DailyBankState _dbstate) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}

	public Operation displayDialog(CompteCourant cpte, CategorieOperation mode) {
		this.categorieOperation = mode;
		this.compteEdite = cpte;
		ObservableList<String> list;

		switch (mode) {
		case DEBIT:

			String info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);

			this.lblType.setText("Type d'opération");
			this.btnOk.setText("Effectuer Débit");
			this.btnCancel.setText("Annuler débit");

			list = FXCollections.observableArrayList();

			for (String tyOp : ConstantesIHM.OPERATIONS_DEBIT_GUICHET) {
				list.add(tyOp);
			}

			this.cbTypeOpe.setItems(list);
			this.cbTypeOpe.getSelectionModel().select(0);
			break;
		case CREDIT:

			String info2 = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info2);

			this.lblType.setText("Type d'opération");
			this.btnOk.setText("Effectuer Crédit");
			this.btnCancel.setText("Annuler Crédit");

			list = FXCollections.observableArrayList();

			for (String tyOp : ConstantesIHM.OPERATIONS_CREDIT_GUICHET) {
				list.add(tyOp);
			}

			this.cbTypeOpe.setItems(list);
			this.cbTypeOpe.getSelectionModel().select(0);
			break;
		case VIREMENT:

			String info3 = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info3);
			
			this.lblType.setText("Les Clients");
			this.btnOk.setText("Effectuer un virement");
			this.btnCancel.setText("Annuler le virement");

			list = FXCollections.observableArrayList();
			
			try {
				for (CompteCourant tyOp : getToutClients(this.compteEdite.idNumCli)) {
					list.add(tyOp.toString());
				}
			} catch (DataAccessException | DatabaseConnexionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			this.cbTypeOpe.setItems(list);
			this.cbTypeOpe.getSelectionModel().select(0);
			break;
			
		}

		// Paramétrages spécifiques pour les chefs d'agences
		if (ConstantesIHM.isAdmin(this.dbs.getEmpAct())) {
			// rien pour l'instant
		}

		this.operationResultat = null;
		this.cbTypeOpe.requestFocus();

		this.primaryStage.showAndWait();
		return this.operationResultat;
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions
	@FXML
	private Label lblMessage;
	@FXML
	private Label lblMontant;
	@FXML
	private Label lblType;
	@FXML
	private ComboBox<String> cbTypeOpe;
	@FXML
	private TextField txtMontant;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.operationResultat = null;
		this.primaryStage.close();
	}

	@FXML
	private void doAjouter() {
		double montant;
		String info;
		String typeOp;
		switch (this.categorieOperation) {
		case VIREMENT:
			// règles de validation d'un débit :
			// - le montant doit être un nombre valide
			// - et si l'utilisateur n'est pas chef d'agence,
			// - le débit ne doit pas amener le compte en dessous de son découvert autorisé
			

			this.txtMontant.getStyleClass().remove("borderred");
			this.lblMontant.getStyleClass().remove("borderred");
			this.lblType.getStyleClass().remove("borderred");
			this.lblMessage.getStyleClass().remove("borderred");
			info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);

			try {
				montant = Double.parseDouble(this.txtMontant.getText().trim());
				if (montant <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException nfe) {
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblType.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			if (this.compteEdite.solde - montant < this.compteEdite.debitAutorise ) {
				info = "Dépassement du découvert ! - Cpt. : " + this.compteEdite.idNumCompte + "  "
						+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
						+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
				this.lblMessage.setText(info);
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblType.getStyleClass().add("borderred");
				this.lblMessage.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			typeOp = this.cbTypeOpe.getValue();
			this.operationResultat = new Operation(-1, montant, null, null, this.compteEdite.idNumCli, typeOp);
			this.primaryStage.close();
			break;
		case DEBIT:
			// règles de validation d'un débit :
			// - le montant doit être un nombre valide
			// - et si l'utilisateur n'est pas chef d'agence,
			// - le débit ne doit pas amener le compte en dessous de son découvert autorisé
			

			this.txtMontant.getStyleClass().remove("borderred");
			this.lblMontant.getStyleClass().remove("borderred");
			this.lblType.getStyleClass().remove("borderred");
			this.lblMessage.getStyleClass().remove("borderred");
			info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);

			try {
				montant = Double.parseDouble(this.txtMontant.getText().trim());
				if (montant <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException nfe) {
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblType.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			if (this.compteEdite.solde - montant < this.compteEdite.debitAutorise && !this.dbs.isChefDAgence() ) {
				info = "Dépassement du découvert ! - Cpt. : " + this.compteEdite.idNumCompte + "  "
						+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
						+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
				this.lblMessage.setText(info);
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblType.getStyleClass().add("borderred");
				this.lblMessage.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			typeOp = this.cbTypeOpe.getValue();
			this.operationResultat = new Operation(-1, montant, null, null, this.compteEdite.idNumCli, typeOp);
			this.primaryStage.close();
			break;
		case CREDIT:
			
			

			this.txtMontant.getStyleClass().remove("borderred");
			this.lblMontant.getStyleClass().remove("borderred");
			this.lblType.getStyleClass().remove("borderred");

			this.lblMessage.getStyleClass().remove("borderred");
			info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);

			try {
				montant = Double.parseDouble(this.txtMontant.getText().trim());
				if (montant <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException nfe) {
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblType.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			
				typeOp = this.cbTypeOpe.getValue();
				this.operationResultat = new Operation(-1, montant, null, null, this.compteEdite.idNumCli, typeOp);
				this.primaryStage.close();
				break;
				
}
	}
	public ArrayList<CompteCourant> getToutClients(int numeroClient)	throws DataAccessException, DatabaseConnexionException {
		ArrayList<CompteCourant> alResult = new ArrayList<>();

		try {
			Connection con = LogToDatabase.getConnexion();

			PreparedStatement pst;

			String query;
			
				query = "SELECT c.* FROM COMPTECOURANT c,CLIENT c2 WHERE c.IDNUMCLI = c2.IDNUMCLI AND c2.IDNUMCLI = "+numeroClient;
				
				pst = con.prepareStatement(query);
			

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				int idNumCompte = rs.getInt("idNumCompte");
				int debitAutorise = rs.getInt("debitAutorise");
				double solde = rs.getDouble("solde");
				String estCloture = rs.getString("estCloture");
				int idNumCli = rs.getInt("idNumCli");


				alResult.add(
						new CompteCourant(idNumCompte,debitAutorise,solde,estCloture,idNumCli));
			}
			rs.close();
			pst.close();
		} catch (SQLException e) {
			throw new DataAccessException(Table.Client, Order.SELECT, "Erreur accès", e);
		}

		return alResult;
	}
	public ArrayList<Operation> getToutOpeations(int numeroClient)	throws DataAccessException, DatabaseConnexionException {
		ArrayList<Operation> alResult = new ArrayList<>();

		try {
			Connection con = LogToDatabase.getConnexion();

			PreparedStatement pst;

			String query;
			
				query = "SELECT o.* FROM OPERATION o, COMPTECOURANT c WHERE o.IDNUMCOMPTE = c.IDNUMCOMPTE  AND o.DATEOP >= SYSDATE - 30 AND c.IDNUMCOMPTE LIKE "+numeroClient;
				
				pst = con.prepareStatement(query);
			

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				
				int idOperation = rs.getInt(1);
				double montant = rs.getDouble(2);
				Date dateOp = rs.getDate(3);
				Date dateValeur = rs.getDate(4);
				int idNumCompte = rs.getInt(5);
				String idTypeOp = rs.getString(6);
				
			


				alResult.add(
						new Operation(idOperation,montant,dateOp,dateValeur,idNumCompte,idTypeOp));
			}
			rs.close();
			pst.close();
		} catch (SQLException e) {
			throw new DataAccessException(Table.Operation, Order.SELECT, "Erreur accès", e);
		}

		return alResult;
	}
	public int getSelection() {
		System.out.println( this.cbTypeOpe.getSelectionModel().getSelectedIndex());

		return this.cbTypeOpe.getSelectionModel().getSelectedIndex();

	}
}
