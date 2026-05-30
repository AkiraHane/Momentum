package com.akirahane.momentum.core.effect;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.phys.Vec3;

// 临时变量
@Getter
@Setter
@Accessors(chain = true)
public class MomentumEffect {

    // 数值
    private Vec3 value;
    // 每Tick变化数值
    private Vec3 modifyValue;
    // 效果类型
    private EffectType type;
    // 持续Tick
    private int duration;
    // 已持续Tick
    private int elapsedDuration;

    public enum EffectType {
        // 替换
        REPLACE,
        // 增加基础数值的倍率
        BASE_MULTIPLIER,
        // 局部坐标数值(以输入向量水平投影为前方)
        LOCAL_VALUE,
        // 合成(正常的世界坐标相加)
        COMPOSE,
        // 倍率
        MULTIPLIER;

        // 返回优先级顺序
        public int getPriority() {
            return switch (this) {
                case REPLACE -> 0;
                case BASE_MULTIPLIER -> 1;
                case MULTIPLIER -> 2;
                case LOCAL_VALUE -> 3;
                case COMPOSE -> 4;
            };
        }
    }

    public MomentumEffect() {
        this.init();
    }

    public void init() {
        this.value = Vec3.ZERO;
        this.modifyValue = Vec3.ZERO;
        this.type = EffectType.LOCAL_VALUE;
        this.duration = 0;
        this.elapsedDuration = 0;
    }

    public void tick() {
        if (!Vec3.ZERO.equals(this.modifyValue)) {
            this.value = this.value.add(this.modifyValue);
        }
        this.elapsedDuration++;
    }

    public MomentumEffect(Vec3 value, Vec3 modifyValue, EffectType type, int duration) {
        this.value = value;
        this.modifyValue = modifyValue;
        this.type = type;
        this.duration = duration;
    }

    // 一维向量
    public double applyTo(double number) {
        if (this.value == null) return number;
        return switch (this.type) {
            case REPLACE -> this.value.x;
            case LOCAL_VALUE -> number + this.value.x;
            case BASE_MULTIPLIER, MULTIPLIER -> number * this.value.x;
            case COMPOSE -> number + this.value.length();
        };
    }

    // 二维向量 暂时没必要写 好累喵 但好兴奋喵

    // 三维向量 向量垂直XZ轴面的时候无法确定轴向 会除零错误 需要玩家角度确定XZ轴方向
    public Vec3 applyTo(Vec3 vec, float yRot) {
        switch (this.type) {
            case REPLACE: {
                return value;
            }
            case LOCAL_VALUE: {
                // value以输入向量的水平投影为X轴方向
                // value.x = 前后(沿水平投影方向), value.y = 上下, value.z = 左右(垂直于水平投影方向)
                double horizontalLength = vec.horizontalDistance();

                double forwardX, forwardZ, rightX, rightZ;

                if (horizontalLength > 1.0E-6) {
                    // 水平投影非零, 以水平投影方向为前方
                    forwardX = vec.x / horizontalLength;
                    forwardZ = vec.z / horizontalLength;
                } else {
                    // 向量垂直(水平投影为零), 按玩家视角方向定前后左右
                    double yRotRad = Math.toRadians(yRot);
                    forwardX = -Math.sin(yRotRad);
                    forwardZ = Math.cos(yRotRad);
                }

                // 右方向: 前方向顺时针旋转90度
                rightX = -forwardZ;
                rightZ = forwardX;

                // 将value从局部坐标系转换到世界坐标系
                double addX = this.value.x * forwardX + this.value.z * rightX;
                double addY = this.value.y;
                double addZ = this.value.x * forwardZ + this.value.z * rightZ;

                return vec.add(addX, addY, addZ);
            }
            case MULTIPLIER, BASE_MULTIPLIER: {
                // 倍率类型: value代表X向量三个方向需要乘以的倍率
                return new Vec3(
                        vec.x * this.value.x,
                        vec.y * this.value.y,
                        vec.z * this.value.z
                );
            }
            case COMPOSE: {
                // 合成类型: 将value和X向量合成为新向量(相加)
                return vec.add(this.value);
            }
            default:
                return vec;
        }
    }
}
