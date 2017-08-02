package findviewbyid.entitys;

import java.awt.LayoutManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Created by wangzai on 2016/11/24.
 */
public class IdBean extends JPanel {
    private JCheckBox  mEnableCheckBox;
    private JLabel     mIdJLabel;
    private JCheckBox  mClickCheckBox;
    private JTextField mFieldJTextField;
    private int        mFileType;

    /**
     * mEnableCheckBox接口
     */
    public interface EnableActionListener {
        void setEnable(JCheckBox enableCheckBox);
    }

    private EnableActionListener mEnableListener;

    public void setEnableActionListener(EnableActionListener enableActionListener) {
        mEnableListener = enableActionListener;
    }

    /**
     * mFieldJTextField接口
     */
    public interface FieldFocusListener {
        void setFieldName(JTextField fieldJTextField);
    }

    private FieldFocusListener mFieldFocusListener;

    public void setFieldFocusListener(FieldFocusListener fieldFocusListener) {
        mFieldFocusListener = fieldFocusListener;
    }

    /**
     * mClickCheckBox接口
     */
    public interface ClickActionListener {
        void setClick(JCheckBox clickCheckBox);
    }

    private ClickActionListener mClickListener;

    public void setClickActionListener(ClickActionListener clickListener) {
        mClickListener = clickListener;
    }

    /**
     * 构造方法
     *
     * @param layout         布局
     * @param emptyBorder    border
     * @param jCheckBox      是否生成+name
     * @param jLabelId       id
     * @param jCheckBoxClick onClick
     * @param jTextField     字段名
     * @param enable         是否生成
     * @param clickable      clickable
     * @param clickEnable    是否Enable
     * @param fileType       类型 0Activity 1Fragment 2Adapter 3其他
     */
    public IdBean(LayoutManager layout, EmptyBorder emptyBorder,
                  JCheckBox jCheckBox, JLabel jLabelId, JCheckBox jCheckBoxClick, JTextField jTextField,
                  boolean enable, boolean clickable, boolean clickEnable, int fileType) {
        super(layout);
        initLayout(layout, emptyBorder);
        mEnableCheckBox = jCheckBox;
        mIdJLabel = jLabelId;
        mClickCheckBox = jCheckBoxClick;
        mFieldJTextField = jTextField;
        mFileType = fileType;
        initComponent(enable, clickable, clickEnable);
        addComponent();
    }

    /**
     * addComponent
     */
    private void addComponent() {
        this.add(mEnableCheckBox);
        this.add(mIdJLabel);
        this.add(mClickCheckBox);
        this.add(mFieldJTextField);
    }

    /**
     * 设置Component
     *
     * @param enable      enable
     * @param clickable   clickable
     * @param clickEnable clickEnable
     */
    private void initComponent(boolean enable, boolean clickable, boolean clickEnable) {
        mEnableCheckBox.setSelected(enable);
        if (mFileType > 1) {
            mClickCheckBox.setVisible(false);
            mClickCheckBox.setEnabled(false);
        } else {
            if (clickEnable) {
                mClickCheckBox.setSelected(clickable);
                mClickCheckBox.setEnabled(enable);
            } else {
                mClickCheckBox.setEnabled(false);
            }
        }
        
        mIdJLabel.setEnabled(enable);
        mFieldJTextField.setEnabled(enable);

        // 设置左对齐
        mEnableCheckBox.setHorizontalAlignment(JLabel.LEFT);
        mIdJLabel.setHorizontalAlignment(JLabel.LEFT);
        mFieldJTextField.setHorizontalAlignment(JTextField.LEFT);
        // 监听
        mEnableCheckBox.addActionListener(e -> {
            if (mEnableListener != null) {
                mEnableListener.setEnable(mEnableCheckBox);
                mIdJLabel.setEnabled(mEnableCheckBox.isSelected());
                if (clickEnable)
                    mClickCheckBox.setEnabled(mEnableCheckBox.isSelected());
                mFieldJTextField.setEnabled(mEnableCheckBox.isSelected());
            }
        });
        // 监听
        mClickCheckBox.addActionListener(e -> {
            if (mClickListener != null) {
                mClickListener.setClick(mClickCheckBox);
            }
        });
        // 监听
        mFieldJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (mFieldFocusListener != null) {
                    mFieldFocusListener.setFieldName(mFieldJTextField);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (mFieldFocusListener != null) {
                    mFieldFocusListener.setFieldName(mFieldJTextField);
                }
            }
        });
    }

    /**
     * 设置布局相关
     *
     * @param layout      layout
     * @param emptyBorder emptyBorder
     */
    private void initLayout(LayoutManager layout, EmptyBorder emptyBorder) {
        // 设置布局内容
        this.setLayout(layout);
        // 设置border
        this.setBorder(emptyBorder);
    }
}
