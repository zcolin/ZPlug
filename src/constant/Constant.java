package constant;

public final class Constant {
    public static final String selectedTextSUFFIX      = ".xml";
    public static final String psiMethodByOnCreate     = "onCreate";
    public static final String psiMethodByOnCreateView = "createView";
    public static final String creatorCommandName      = "Generate Injections";
    public static final String FieldOnClick            = "OnClick";

    public static final class actions {
        public static final String selectedMessage         = "布局内容：（不需要输入R.layout.）";
        public static final String selectedTitle           = "未选中布局内容，请输入layout文件名";
        public static final String selectedErrorNoName     = "未输入layout文件名";
        public static final String selectedErrorNoSelected = "未找到选中的布局文件";
        public static final String selectedErrorNoId       = "未找到任何Id";
        public static final String selectedErrorNoPoint    = "光标未在Class内";
        public static final String selectedSuccess         = "生成成功";
    }

    public static final class dialogs {
        public static final String titleFindViewById    = "FindViewByIdDialog";
        public static final String tableFieldViewWidget = "Widget";
        public static final String tableFieldViewId     = "ViewId";
        public static final String viewHolderCheck      = "Create ViewHolder";
        public static final String buttonConfirm        = "确定";
        public static final String buttonCancel         = "取消";
    }

    public static final class utils {
        public static final String creatorInitViewName         = "initView";
        public static final String creatorSetContentViewMethod = "setContentView";
    }

}
