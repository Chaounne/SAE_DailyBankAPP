package model.orm;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import model.data.PrelevementAutomatique;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.ManagementRuleViolation;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

public class AccessPrelevement {
	
	
	public AccessPrelevement() {
	}

	/**
	 * Recherche de toutes les prélvements d'un compte.
	 *
	 * @param idNumCompte id du compte dont on cherche toutes les prélvements
	 * @return Toutes les prélvements du compte, liste vide si pas de prélvement
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public ArrayList<PrelevementAutomatique> getPrelevements(int idNumCompte) throws DataAccessException, DatabaseConnexionException {
		ArrayList<PrelevementAutomatique> alResult = new ArrayList<>();

		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM PrelevementAutomatique where idNumCompte = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idNumCompte);	

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				int idPrelevement = rs.getInt("idPrelev");
				double montant = rs.getDouble("montant");
				String dateRecurrente = rs.getString("dateRecurrente");
				String beneficiaire = rs.getString("beneficiaire");
				int idNumCompteTrouve = rs.getInt("idNumCompte");

				alResult.add(new PrelevementAutomatique(idPrelevement, montant, dateRecurrente, beneficiaire, idNumCompteTrouve));
			}
			rs.close();
			pst.close();
			return alResult;
		} catch (SQLException e) {
			throw new DataAccessException(Table.PrelevementAutomatique, Order.SELECT, "Erreur accès", e);
		}
	}

	/**
	 * Recherche d'un prélvement par son id.
	 *
	 * @param idOperation id de le prélvement recherchée (clé primaire)
	 * @return un prelevement ou null si non trouvé
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public PrelevementAutomatique getPrelevement(int idPrelevement)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		PrelevementAutomatique prelevementTrouve;

		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM PrelevementAutomatique where" + " idPrelev = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idPrelevement);

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				int idPrelevementTrouve = rs.getInt("idPrelevement");
				double montant = rs.getDouble("montant");
				String dateRecurrente = rs.getString("dateRecurrente");
				String beneficiaire = rs.getString("beneficiaire");
				int idNumCompteTrouve = rs.getInt("idNumCompte");

				prelevementTrouve = new PrelevementAutomatique(idPrelevement, montant, dateRecurrente, beneficiaire, idNumCompteTrouve);
			} else {
				rs.close();
				pst.close();
				return null;
			}

			if (rs.next()) {
				rs.close();
				pst.close();
				throw new RowNotFoundOrTooManyRowsException(Table.PrelevementAutomatique, Order.SELECT,
						"Recherche anormale (en trouve au moins 2)", null, 2);
			}
			rs.close();
			pst.close();
			return prelevementTrouve;
		} catch (SQLException e) {
			throw new DataAccessException(Table.PrelevementAutomatique, Order.SELECT, "Erreur accs", e);
		}
	}

	/**
	 * Enregistrement d'un débit.
	 *
	 * Se fait par procédure stockée : - Vérifie que le débitAutorisé n'est pas
	 * dépassé - Enregistre l'prélvement - Met Ã  jour le solde du compte.
	 *
	 * @param idNumCompte compte débité
	 * @param montant     montant débité
	 * @param typeOp      libellé de l'prélvement effectuée (cf TypeOperation)
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 * @throws ManagementRuleViolation
	 */
	public void ajouterPrelevementAuto(double montant, String dateRecurrente, String beneficiaire, int idNumCompte)
			throws DatabaseConnexionException, ManagementRuleViolation, DataAccessException {
		try {
			Connection con = LogToDatabase.getConnexion();
			CallableStatement call;

			String q = "INSERT INTO PrelevementAutomatique (idPrelev, montant, dateRecurrente, beneficiaire, idNumCompte) VALUES (seq_id_client.NEXTVAL, ?, ?, ?, ?)";
			// les ? correspondent aux paramètres : cf. déf procédure (4 paramÃ¨tres)
			call = con.prepareCall(q);
			// Paramètres in
			call.setDouble(1, montant);
			// 1 -> valeur du premier paramètre, cf. déf procédure
			call.setString(2, dateRecurrente);
			call.setString(3, beneficiaire);
			call.setInt(4, idNumCompte);
			// Paramètres out
			call.registerOutParameter(4, java.sql.Types.INTEGER);
			// 4 type du quatrième paramètre qui est déclaré en OUT, cf. déf procédure

			call.execute();

			int res = call.getInt(4);

		} catch (SQLException e) {
			throw new DataAccessException(Table.Operation, Order.INSERT, "Erreur accÃ¨s", e);
		}
	}
	
	
	public void updatePrelev(PrelevementAutomatique prlv)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {
			Connection con = LogToDatabase.getConnexion();

			String query = "UPDATE PrelevementAutomatique SET " +  "montant = " + "? , " + "dateRecurrente = "
					+ "? , " + "beneficiaire = " + "?  " + "WHERE idPrelev = " + "? " ;

			PreparedStatement pst = con.prepareStatement(query);
			pst.setDouble(1, prlv.montant);
			System.out.println(prlv.montant);
			pst.setString(2, prlv.dateRecurrente);
			pst.setString(3, prlv.beneficiaire);
			pst.setInt(4, prlv.idPrelev);

			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();
			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.Client, Order.UPDATE,
						"Update anormal (update de moins ou plus d'une ligne)", null, result);
			}
			con.commit();
		} catch (SQLException e) {
			throw new DataAccessException(Table.Client, Order.UPDATE, "Erreur accès", e);
		}
	}
	
	public void deletePrelev(PrelevementAutomatique prlv)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {
			Connection con = LogToDatabase.getConnexion();

			String query = "UPDATE PrelevementAutomatique SET " +  "montant = " + "0 , " + "dateRecurrente = "
					+ "0 " + "WHERE idPrelev = " + "? " ;

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, prlv.idPrelev);

			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();
			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.Client, Order.UPDATE,
						"Update anormal (update de moins ou plus d'une ligne)", null, result);
			}
			con.commit();
		} catch (SQLException e) {
			throw new DataAccessException(Table.Client, Order.UPDATE, "Erreur accès", e);
		}
	}

	
	public void executerPrelevAuto() throws DataAccessException {

        if(!isPrelevedToday()) {
            try {
                Connection con = LogToDatabase.getConnexion();
                CallableStatement call;

                String q = "{call ExecuterPrelevAuto (?)}";

                call = con.prepareCall(q);

                call.registerOutParameter(1, Types.VARCHAR);

                call.execute();

                String res = call.getString(1);
                
                System.out.println(res);
                
                if(res != null)
                    System.out.println(res);

            }catch (SQLException e) {
                throw new DataAccessException(Table.Operation, Order.INSERT, "Erreur accès", e);
            } catch (DatabaseConnexionException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isPrelevedToday() throws DataAccessException {
        try {
            Connection con = LogToDatabase.getConnexion();
            CallableStatement call;

            String q = "SELECT TO_CHAR(DATEOP, 'DD/MM/YYYY') FROM PrelevementAutomatique P, OPERATION O " +
                    "WHERE P.DATERECURRENTE = TO_CHAR(SYSDATE, 'DD') AND O.IDNUMCOMPTE = P.IDNUMCOMPTE " +
                    "AND idTypeOp = 'Prélvement automatique' AND TO_CHAR(DATEOP, 'DD/MM/YYYY') = TO_CHAR(SYSDATE, 'DD/MM/YYYY')";

            call = con.prepareCall(q);

            ResultSet rs = call.executeQuery();

            if(rs.next())
                return true;

        }catch (SQLException e) {
            throw new DataAccessException(Table.Operation, Order.INSERT, "Erreur accs", e);
        } catch (DatabaseConnexionException e) {
            e.printStackTrace();
        }
        return false;
    }

}
