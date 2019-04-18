package com.github.suayy.bowling.split;

import com.github.sunayy.actionexec.Action;
import com.github.sunayy.actionexec.executor.ActionExecutor;
import com.github.sunayy.actionexec.executor.Result;
import com.github.sunayy.actionexec.handle.ExceptionHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * ボウリングのsplit判定プログラム。
 * 階差数列を考えたらもっとシンプルに書けそう
 */
public class Main {

    private static final String MESSAGE_TOO_MANY        = "too many arguments";
    private static final String MESSAGE_STRIKE          = "Strike!";
    private static final String MESSAGE_GUTTER          = "Gutter!";
    private static final String MESSAGE_REMAINED_ONLY_ONE    = "there's only one left";

    private static final int MAX_PIN_NUM = 10;

    public static void main(String[] args) {

        final Optional<String> decisionUnnecessaryMessage
                = getMessageWhenDecisionUnnecessary(args);

        // コマンドライン引数長によってはここで処理を打ち切る
        if (decisionUnnecessaryMessage.isPresent()) {
            System.out.println(decisionUnnecessaryMessage.get());
            return;
        }

        // 例外時挙動…①
        final Consumer<IllegalPinException> exceptionConsumer
                = e -> System.out.println(e.getMessage());
        // Throwableな処理定義…②
        final Action<Boolean[], IllegalPinException> getRemainsArray
                = () -> convertToSplitDecisionArrayFrom(args);
        // ②を実行した際に起こり得る例外のハンドラーインスタンス
        final ExceptionHandler<IllegalPinException> handler
                = new ExceptionHandler<>(IllegalPinException.class, exceptionConsumer);
        // ②を実行と例外捕捉を↓が担う
        final ActionExecutor<Boolean[], IllegalPinException> executor
                = new ActionExecutor<>(getRemainsArray, handler);

        // ②の実行
        final Result<Boolean[], IllegalPinException> result = executor.action();

        // 例外発生していたら終わり
        if (!result.isRight()) {
            return;
        }

        // split判定。厳密に書けばもっと効率良くなる
        final boolean isSplit = result.map(Arrays::asList)
                                .map(List::stream)
                                .map(s -> s.map(b -> b ? "1" : "0"))
                                .map(s -> s.collect(Collectors.joining()))
                                .map(s -> s.matches(".*10+1.*"))
                                .unwrapSucceeded()
                                .orElse(false);

        System.out.println("remained pins are" + (isSplit ? " " : " not ") + "split");
    }

    private static Optional<String> getMessageWhenDecisionUnnecessary(String[] args) {
        final int remains = args.length;
        if (remains > PIN.MAX_REMAINED) {
            return Optional.of(MESSAGE_TOO_MANY);
        } else switch (args.length) {
            case PIN.MAX_REMAINED:  return Optional.of(MESSAGE_GUTTER);
            case PIN.MIN_REMAINED:  return Optional.of(MESSAGE_REMAINED_ONLY_ONE);
            case PIN.NO_REMAINING:  return Optional.of(MESSAGE_STRIKE);
            default:                return Optional.empty();
        }
    }

    /**
     * split判定用の配列を得る１</br>
     * 定義として、ピンを７列で縦割りした時に残りの間隔が開いているものが該当するため、
     * それを判別するためのもの。</br>
     * ただし、先頭のピンが残っている場合はその時点でsplitではないことが明らかであるため
     * その場合は場合分けのため長さ１、偽の要素が入った配列を返す</br>
     * @param inputRemains 残ピン配置
     * @return １番ピンが残っていた場合は { false }、それ以外は対応する列のピンの有無を示す配列
     */
    private static Boolean[] convertToSplitDecisionArrayFrom(final String[] inputRemains) {
        Boolean[] remains = new Boolean[PIN.values().length];
        Arrays.fill(remains, 0);

        for (int i = 0; i < inputRemains.length; i++) {
            final int pinNum;
            try {
                pinNum = Integer.parseInt(inputRemains[i]);
            } catch (NumberFormatException e) {
                throw new IllegalPinException(IllegalPinException.ILLEGAL_VALUE);
            }
            if (pinNum < 0) {
                throw new IllegalPinException(IllegalPinException.NUMBER_TOO_SMALL);
            }
            if (pinNum > MAX_PIN_NUM) {
                throw new IllegalPinException(IllegalPinException.NUMBER_TOO_BIG);
            }
            // この実装は好かないけど、計算量を抑えるため。。。
            if (pinNum == PIN.FIRST_PIN_NUMBER) {
                return new Boolean[]{ Boolean.FALSE };
            }
            remains[PIN.get(pinNum).getColNum()] = true;
        }
        return remains;
    }

    /**
     * ピンを表す列挙
     * ただ、配置には規則性があるから列挙するのは回りくどい気が。。
     */
    private enum PIN {
        FIRST, SECOND, THIRD, FOURTH, FIFTH, SIXTH, SEVENTH, EIGHTH, NINTH, TENTH;

        static PIN get(int index) { return values()[index - 1]; }

        final static int FIRST_PIN_NUMBER   = 1;
        final static int MAX_REMAINED       = 10;
        final static int MIN_REMAINED       = 1;
        final static int NO_REMAINING       = 0;

        final static int SEVENTH_IDX        = 0;
        final static int FOURTH_IDX         = 1;
        final static int SECOND_EIGHTH_IDX  = 2;
        final static int FIRST_FIFTH_IDX    = 3;
        final static int THIRD_NINTH_IDX    = 4;
        final static int SIXTH_IDX          = 5;
        final static int TENTH_IDX          = 6;

        int getColNum() {
            switch (this) {
                case SEVENTH:   return SEVENTH_IDX;
                case FOURTH:    return FOURTH_IDX;
                case SECOND:
                case EIGHTH:    return SECOND_EIGHTH_IDX;
                case FIRST:
                case FIFTH:     return FIRST_FIFTH_IDX;
                case THIRD:
                case NINTH:     return THIRD_NINTH_IDX;
                case SIXTH:     return SIXTH_IDX;
                default:        return TENTH_IDX;
            }
        }
    }
}
