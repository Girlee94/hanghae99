package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@AutoConfigureMockMvc
public class UsePointTest {
    @Autowired
    private MockMvc mockMvc;

    private PointService pointService;
    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        userPointTable = Mockito.mock(UserPointTable.class);
        pointHistoryTable = Mockito.mock(PointHistoryTable.class);
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("0원 사용 요청")
    void givenZeroUsePoint_whenUseUserPoint() {
        long userId = 10L;
        long amount = 0L;

        assertThrows(IllegalArgumentException.class, () -> pointService.useUserPoint(userId, amount));
    }

    @Test
    @DisplayName("유효하지 않은 회원 정보 요청")
    void givenUnvalidUserId_henUseUserPoint() {
        long userId = 10L;
        long amount = 100L;

        assertThrows(NoSuchElementException.class, () -> pointService.useUserPoint(userId, amount));
    }

    @Test
    @DisplayName("잔액보다 많은 포인트 사용 요청")
    void givenUsePoint_moreThenLastAmount_whenUseUserPoint() {
        long userId = 10L;
        long lastAmount = 500L;
        long requestAmount = 1000L;

        Mockito.when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, lastAmount, System.currentTimeMillis()));

        CommonResponse response = pointService.useUserPoint(userId, requestAmount);

        assertEquals(response.getCode(), "2001");
    }

    @Test
    @DisplayName("포인트 사용 요청 및 히스토리 기록")
    void givenUserPoint_whenUseUserPoint() {
        long userId = 10L;
        long lastAmount = 5000L;
        long requestAmount = 1000L;
        long finalPoint = lastAmount - requestAmount;
        long chargeTime = System.currentTimeMillis();
        Mockito.when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, lastAmount, System.currentTimeMillis()));
        Mockito.when(userPointTable.insertOrUpdate(userId, finalPoint)).thenReturn(new UserPoint(userId, finalPoint, chargeTime));
        Mockito.when(pointHistoryTable.insert(userId, requestAmount, TransactionType.USE, chargeTime)).thenReturn(new PointHistory(1L, userId, requestAmount, TransactionType.USE, chargeTime));

        //when
        CommonResponse userPointResponse = pointService.useUserPoint(userId, requestAmount);
        UserPoint userPoint = (UserPoint) userPointResponse.getData();

        //then
        assertEquals(finalPoint, userPoint.point());

        Mockito.verify(userPointTable, Mockito.times(1)).selectById(userId);
        Mockito.verify(userPointTable, Mockito.times(1)).insertOrUpdate(userId, finalPoint);

        Mockito.verify(pointHistoryTable, Mockito.times(1)).insert(eq(userId), eq(requestAmount), eq(TransactionType.USE), anyLong());
    }
}
