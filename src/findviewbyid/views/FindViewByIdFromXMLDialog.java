package findviewbyid.views;

/**
 * FindViewByIdFromJavaDialog
 */
public class FindViewByIdFromXMLDialog extends FindViewByIdFromJavaDialog {
    private ContentShowDialog mDialog;

    public FindViewByIdFromXMLDialog(Builder builder) {
        super(builder);
    }

    @Override
    protected void actionConfirm() {
        cancelDialog();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancelDialog();
        }
        mDialog = new ContentShowDialog(mElements, mPsiFile, mProject);
        mDialog.showDialog();
    }
}
