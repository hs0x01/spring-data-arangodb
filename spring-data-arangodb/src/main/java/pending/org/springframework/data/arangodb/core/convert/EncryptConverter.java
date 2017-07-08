package pending.org.springframework.data.arangodb.core.convert;

/**
 * 暗号化 / 復号を行うコンバータです。
 * 
 * @author hs0x01
 *
 */
public interface EncryptConverter {
	
	/**
	 * 文字列を暗号化して返します。
	 * 
	 * @param str
	 *            文字列
	 * @return 暗号化された文字列
	 */
	String encrypt(String str);
	
	/**
	 * 暗号化された文字列を復号して返します。
	 * 
	 * @param str
	 *            暗号化された文字列
	 * @return 復号された文字列
	 */
	String decrypt(String str);
}
