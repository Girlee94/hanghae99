package io.hhplus.tdd.point;

import io.hhplus.tdd.CommonResponse;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public CommonResponse getUserPoint(long id) {
        UserPoint userPoint = userPointTable.selectById(id);

        return CommonResponse.success(userPoint);
    }

    public CommonResponse getUserPointHistories(long id) {
        List<PointHistory> userPointHistories = pointHistoryTable.selectAllByUserId(id);

        return CommonResponse.success(userPointHistories);
    }

    public CommonResponse chargeUserPoint(long id, long amount) {
        long chargePoint = getUserLastAmount(id) + amount;
        UserPoint userPoint = insertOrUpdateUserPoint(id, amount, chargePoint, TransactionType.CHARGE);

        return CommonResponse.success(userPoint);
    }

    public CommonResponse useUserPoint(long id, long amount) {
        long lastAmount = getUserLastAmount(id);
        if (lastAmount < amount) {
            return CommonResponse.fail("2001", "잔액이 부족합니다.");
        }

        long updateAmount = lastAmount - amount;
        UserPoint userPoint = insertOrUpdateUserPoint(id, amount, updateAmount, TransactionType.USE);

        return CommonResponse.success(userPoint);
    }

    private long getUserLastAmount(long id) {
        UserPoint userPoint = userPointTable.selectById(id);
        return userPoint.point();
    }

    private UserPoint insertOrUpdateUserPoint(long id, long requestAmount, long updateAmount, TransactionType transactionType) {
        UserPoint userPoint = userPointTable.insertOrUpdate(id, updateAmount);
        pointHistoryTable.insert(id, requestAmount, transactionType, userPoint.updateMillis());
        return userPoint;
    }
}
