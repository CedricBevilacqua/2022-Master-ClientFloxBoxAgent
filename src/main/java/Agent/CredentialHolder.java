package Agent;

/**
 * Classe servant uniquement à contenur les identifiants du compte sur l'API.
 *
 * @author Cédric Bevilacqua, Semae Altinkaynak
 */
public class CredentialHolder {
	private static String username;
	private static String password;
	
	/**
	 * Enregistre les nouveaux identifiants
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param username : Nouvel identifiant
	 * @param password : Nouveau mot de passe
	 */
	public static void Set(String username, String password) {
		CredentialHolder.username = username;
		CredentialHolder.password = password;
	}
	
	/**
	 * Accesseur de l'identifiant.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @return Chaine de caractère de l'identifiant
	 */
	public static String GetUsername() {
		return username;
	}
	
	/**
	 * Accesseur du mot de passe
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @return Chaine de caractère du mot de passe
	 */
	public static String GetPassword() {
		return password;
	}
}