package findviewbyid.entitys;

import com.intellij.psi.xml.XmlTag;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import findviewbyid.utils.Util;

public class Element {

    // 判断id正则
    private static final Pattern sIdPattern = Pattern.compile("@\\+?(android:)?id/([^$]+)$", Pattern.CASE_INSENSITIVE);
    // id
    private String id;
    // 名字如TextView
    private String name;
    // 命名1 aa_bb_cc; 2 aaBbCc 3 mAaBbCc
    private int fieldNameType = 2;
    private String fieldName;
    private XmlTag xml;
    // 是否生成
    private boolean isEnable    = true;
    // 是否有clickable
    private boolean clickEnable = false;
    // 是否Clickable
    private boolean clickable   = false;

    /**
     * 构造函数
     *
     * @param name      View的名字
     * @param id        android:id属性
     * @param clickable clickable
     * @throws IllegalArgumentException When the arguments are invalid
     */
    public Element(String name, String id, boolean clickable, XmlTag xml) {
        // id
        final Matcher matcher = sIdPattern.matcher(id);
        if (matcher.find() && matcher.groupCount() > 1) {
            this.id = matcher.group(2);
        }

        if (this.id == null) {
            throw new IllegalArgumentException("Invalid format of view id");
        }

        String[] packages = name.split("\\.");
        if (packages.length > 1) {
            // com.example.CustomView
            this.name = packages[packages.length - 1];
        } else {
            this.name = name;
        }

        this.clickEnable = true;

        this.clickable = clickable;

        this.xml = xml;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFieldNameType() {
        return fieldNameType;
    }

    public void setFieldNameType(int fieldNameType) {
        this.fieldNameType = fieldNameType;
    }

    public XmlTag getXml() {
        return xml;
    }

    public void setXml(XmlTag xml) {
        this.xml = xml;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public boolean isClickEnable() {
        return clickEnable;
    }

    public void setClickEnable() {
        this.clickEnable = true;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    /**
     * 获取id，R.id.id
     */
    public String getFullID() {
        String rPrefix = "R.id.";
        return rPrefix + id;
    }

    /**
     * 获取变量名
     */
    public String getFieldName() {
        if (StringUtils.isEmpty(this.fieldName)) {
            String fieldName = id;
            String[] names = id.split("_");
            if (fieldNameType == 2) {
                // aaBbCc
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < names.length; i++) {
                    if (i == 0) {
                        sb.append(names[i]);
                    } else {
                        sb.append(Util.firstToUpperCase(names[i]));
                    }
                }
                fieldName = sb.toString();
            } else if (fieldNameType == 3) {
                // mAaBbCc
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < names.length; i++) {
                    if (i == 0) {
                        sb.append("m");
                    }
                    sb.append(Util.firstToUpperCase(names[i]));
                }
                fieldName = sb.toString();
            } else if (fieldNameType == 1) {
                fieldName = id;
            }
            this.fieldName = fieldName;
        }
        return this.fieldName;
    }

    /**
     * 获取变量名
     */
    public String getFirstUpperCaseFieldName() {
        String fieldName = id;
        String[] names = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            sb.append(Util.firstToUpperCase(names[i]));
        }
        fieldName = sb.toString();
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
