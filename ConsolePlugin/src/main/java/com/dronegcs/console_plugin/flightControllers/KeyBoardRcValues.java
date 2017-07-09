package com.dronegcs.console_plugin.flightControllers;

import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by taljmars on 7/8/2017.
 */
public class KeyBoardRcValues {

    private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(KeyBoardRcValues.class);

    /* Simple annotation that mark which object should be serialized to a file */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SerializableKeyboardControllerValue {
        int deValue() default 0;
    }


    @SerializableKeyboardControllerValue(deValue = 300)
    private AtomicInteger _STABILIZER_CYCLE = null;

    @SerializableKeyboardControllerValue(deValue = 1000)
    private AtomicInteger _MIN_PWM_RANGE = null;

    @SerializableKeyboardControllerValue(deValue = 2200)
    private AtomicInteger _MAX_PWM_RANGE = null;

    @SerializableKeyboardControllerValue(deValue = 1100)
    private AtomicInteger _MIN_PWM_ANGLE = null;

    @SerializableKeyboardControllerValue(deValue = 1900)
    private AtomicInteger _MAX_PWM_ANGLE = null;

    @SerializableKeyboardControllerValue(deValue = 1500)
    private AtomicInteger _TRIM_ANGLE = null;

    @SerializableKeyboardControllerValue(deValue = 10)
    private AtomicInteger _PITCH_STEP = null;
    private AtomicInteger _TRIM_ANGLE_PITCH = null;

    @SerializableKeyboardControllerValue(deValue = 10)
    private AtomicInteger _ROLL_STEP = null;
    private AtomicInteger _TRIM_ANGLE_ROLL = null;

    @SerializableKeyboardControllerValue(deValue = 50)
    private AtomicInteger _YAW_STEP = null;
    private AtomicInteger _TRIM_ANGLE_YAW = null;

    @SerializableKeyboardControllerValue(deValue = 25)
    private AtomicInteger _THR_STEP = null;

    @SerializableKeyboardControllerValue(deValue = 1100)
    private AtomicInteger _INIT_THR = null;

    public Integer get_STABILIZER_CYCLE() {
        return _STABILIZER_CYCLE.get();
    }

    public void set_STABILIZER_CYCLE(Integer _STABILIZER_CYCLE) {
        if (this._STABILIZER_CYCLE == null)
            this._STABILIZER_CYCLE = new AtomicInteger(0);
        this._STABILIZER_CYCLE.set(_STABILIZER_CYCLE);
    }

    public Integer get_MIN_PWM_RANGE() {
        return _MIN_PWM_RANGE.get();
    }

    public void set_MIN_PWM_RANGE(Integer _MIN_PWM_RANGE) {
        if (this._MIN_PWM_RANGE == null)
            this._MIN_PWM_RANGE = new AtomicInteger(0);
        this._MIN_PWM_RANGE.set(_MIN_PWM_RANGE);
    }

    public Integer get_MAX_PWM_RANGE() {
        return _MAX_PWM_RANGE.get();
    }

    public void set_MAX_PWM_RANGE(Integer _MAX_PWM_RANGE) {
        if (this._MAX_PWM_RANGE == null)
            this._MAX_PWM_RANGE = new AtomicInteger(0);
        this._MAX_PWM_RANGE.set(_MAX_PWM_RANGE);
    }

    public Integer get_MIN_PWM_ANGLE() {
        return _MIN_PWM_ANGLE.get();
    }

    public void set_MIN_PWM_ANGLE(Integer _MIN_PWM_ANGLE) {
        if (this._MIN_PWM_ANGLE == null)
            this._MIN_PWM_ANGLE = new AtomicInteger(0);
        this._MIN_PWM_ANGLE.set(_MIN_PWM_ANGLE);
    }

    public Integer get_MAX_PWM_ANGLE() {
        return _MAX_PWM_ANGLE.get();
    }

    public void set_MAX_PWM_ANGLE(Integer _MAX_PWM_ANGLE) {
        if (this._MAX_PWM_ANGLE == null)
            this._MAX_PWM_ANGLE = new AtomicInteger(0);
        this._MAX_PWM_ANGLE.set(_MAX_PWM_ANGLE);
    }

    public Integer get_TRIM_ANGLE() {
        return _TRIM_ANGLE.get();
    }

    public void set_TRIM_ANGLE(Integer _TRIM_ANGLE) {
        if (this._TRIM_ANGLE == null)
            this._TRIM_ANGLE = new AtomicInteger(0);
        this._TRIM_ANGLE.set(_TRIM_ANGLE);

        set_TRIM_ANGLE_PITCH(_TRIM_ANGLE);
        set_TRIM_ANGLE_ROLL(_TRIM_ANGLE);
        set_TRIM_ANGLE_YAW(_TRIM_ANGLE);
    }

    public Integer get_PITCH_STEP() {
        return _PITCH_STEP.get();
    }

    public void set_PITCH_STEP(Integer _PITCH_STEP) {
        if (this._PITCH_STEP == null)
            this._PITCH_STEP = new AtomicInteger(0);
        this._PITCH_STEP.set(_PITCH_STEP);
    }

    public Integer get_TRIM_ANGLE_PITCH() {
        return _TRIM_ANGLE_PITCH.get();
    }

    public void set_TRIM_ANGLE_PITCH(Integer _TRIM_ANGLE_PITCH) {
        if (this._TRIM_ANGLE_PITCH == null)
            this._TRIM_ANGLE_PITCH = new AtomicInteger(0);
        this._TRIM_ANGLE_PITCH.set(_TRIM_ANGLE_PITCH);
    }

    public Integer get_ROLL_STEP() {
        return _ROLL_STEP.get();
    }

    public void set_ROLL_STEP(Integer _ROLL_STEP) {
        if (this._ROLL_STEP == null)
            this._ROLL_STEP = new AtomicInteger(0);
        this._ROLL_STEP.set(_ROLL_STEP);
    }

    public Integer get_TRIM_ANGLE_ROLL() {
        return _TRIM_ANGLE_ROLL.get();
    }

    public void set_TRIM_ANGLE_ROLL(Integer _TRIM_ANGLE_ROLL) {
        if (this._TRIM_ANGLE_ROLL == null)
            this._TRIM_ANGLE_ROLL = new AtomicInteger(0);
        this._TRIM_ANGLE_ROLL.set(_TRIM_ANGLE_ROLL);
    }

    public Integer get_YAW_STEP() {
        return _YAW_STEP.get();
    }

    public void set_YAW_STEP(Integer _YAW_STEP) {
        if (this._YAW_STEP == null)
            this._YAW_STEP = new AtomicInteger(0);
        this._YAW_STEP.set(_YAW_STEP);
    }

    public Integer get_TRIM_ANGLE_YAW() {
        return _TRIM_ANGLE_YAW.get();
    }

    public void set_TRIM_ANGLE_YAW(Integer _TRIM_ANGLE_YAW) {
        if (this._TRIM_ANGLE_YAW == null)
            this._TRIM_ANGLE_YAW = new AtomicInteger(0);
        this._TRIM_ANGLE_YAW.set(_TRIM_ANGLE_YAW);
    }

    public Integer get_THR_STEP() {
        return _THR_STEP.get();
    }

    public void set_THR_STEP(Integer _THR_STEP) {
        if (this._THR_STEP == null)
            this._THR_STEP = new AtomicInteger(0);
        this._THR_STEP.set(_THR_STEP);
    }

    public Integer get_INIT_THR() {
        return _INIT_THR.get();
    }

    public void set_INIT_THR(Integer _INIT_THR) {
        if (this._INIT_THR == null)
            this._INIT_THR = new AtomicInteger(0);
        this._INIT_THR.set(_INIT_THR);
    }

    public boolean isInitialized() {
        if (
            _STABILIZER_CYCLE == null ||
            _MIN_PWM_RANGE == null ||
            _MAX_PWM_RANGE == null ||
            _MIN_PWM_ANGLE == null ||
            _MAX_PWM_ANGLE == null ||
            _TRIM_ANGLE == null ||
            _PITCH_STEP == null ||
            _TRIM_ANGLE_PITCH == null ||
            _ROLL_STEP == null ||
            _TRIM_ANGLE_ROLL == null ||
            _YAW_STEP == null ||
            _TRIM_ANGLE_YAW == null ||
            _THR_STEP == null ||
            _INIT_THR == null
        )
            return false;

        return true;
    }

    public static boolean isSerializable(Field field) {
        return field.isAnnotationPresent(KeyBoardRcValues.SerializableKeyboardControllerValue.class);
    }

    public static Object execGetter(KeyBoardRcValues keyBoardRcValues, Field field) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getter = KeyBoardRcValues.class.getDeclaredMethod("get" + field.getName());
        return getter.invoke(keyBoardRcValues);
    }

    public static void execSetter(KeyBoardRcValues keyBoardRcValues, Field field, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method setter = KeyBoardRcValues.class.getDeclaredMethod("set" + field.getName(), Integer.class);
        setter.invoke(keyBoardRcValues, value);
    }

    public static KeyBoardRcValues generateDefault() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        KeyBoardRcValues res = new KeyBoardRcValues();
        Field[] fields = KeyBoardRcValues.class.getDeclaredFields();
        for (Field field : fields) {
            if (!isSerializable(field))
                continue;

            SerializableKeyboardControllerValue[] annot = field.getAnnotationsByType(SerializableKeyboardControllerValue.class);
            if (annot.length != 1)
                continue;

            execSetter(res, field, annot[0].deValue());
        }
        System.err.println(res);
        return res;
    }

    @Override
    public String toString() {
        return "KeyBoardRcValues{" +
                "_STABILIZER_CYCLE=" + _STABILIZER_CYCLE +
                ", _MIN_PWM_RANGE=" + _MIN_PWM_RANGE +
                ", _MAX_PWM_RANGE=" + _MAX_PWM_RANGE +
                ", _MIN_PWM_ANGLE=" + _MIN_PWM_ANGLE +
                ", _MAX_PWM_ANGLE=" + _MAX_PWM_ANGLE +
                ", _TRIM_ANGLE=" + _TRIM_ANGLE +
                ", _PITCH_STEP=" + _PITCH_STEP +
                ", _TRIM_ANGLE_PITCH=" + _TRIM_ANGLE_PITCH +
                ", _ROLL_STEP=" + _ROLL_STEP +
                ", _TRIM_ANGLE_ROLL=" + _TRIM_ANGLE_ROLL +
                ", _YAW_STEP=" + _YAW_STEP +
                ", _TRIM_ANGLE_YAW=" + _TRIM_ANGLE_YAW +
                ", _THR_STEP=" + _THR_STEP +
                ", _INIT_THR=" + _INIT_THR +
                '}';
    }
}
