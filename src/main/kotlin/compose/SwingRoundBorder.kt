package compose

import androidx.compose.ui.unit.Dp
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Insets
import javax.swing.border.Border


/**
 * Swing
 * 设置圆角边框（可以自定义边框的颜色）
 * 可以为button，文本框等人以组件添加边框
 * 使用方法：
 * JButton close = new JButton(" 关 闭 ");
 * close.setOpaque(false);// 设置原来按钮背景透明
 * close.setBorder(new RoundBorder());黑色的圆角边框
 * close.setBorder(new RoundBorder(Color.RED)); 红色的圆角边框
 *
 * @author Monsoons
 */
class SwingRoundBorder(var roundSize: Dp, private var color: Color = Color.BLACK) : Border {

    override fun getBorderInsets(c: Component): Insets {
        return Insets(0, 0, 0, 0)
    }

    override fun isBorderOpaque(): Boolean {
        return false
    }

    // 实现Border（父类）方法
    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        g.color = color
        g.drawRoundRect(0, 0, c.width - 1, c.height - 1, roundSize.value.toInt(), roundSize.value.toInt())
    }
}