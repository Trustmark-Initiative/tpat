package tmf.host

/**
 * A useful way of analyzing both TD and TIP links which allows for more abstract code.
 * <br/><br/>
 * @user brad
 * @date 12/23/16
 */
interface VersionSetLink {

    public boolean isTdLink();

    public boolean isTipLink();

}