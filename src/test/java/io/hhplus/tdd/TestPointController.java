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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TestPointController {

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
    @DisplayName("유효하지 않은 id로 조회 시 Bad Request 응답")
    void givenInvalidId_whenRequestPoint_thenReturnsBadRequest() throws Exception {
        // given
        long invalidId = 0L;

        // when & then
        mockMvc.perform(get("/point/" + invalidId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 포인트 정보 조회")
    void getUserPoint() {
        // given
        long userId = 1L;
        long amount = 1000L;
        Mockito.when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // when
        CommonResponse response = pointService.getUserPoint(userId);
        UserPoint userPoint = (UserPoint) response.getData();

        // then
        assertEquals(userPoint.id(), userId);
        assertEquals(userPoint.point(), amount);
    }

    @Test
    @DisplayName("0원으로 포인트 충전 요청 시 실패")
    void givenZeroUserPoint_whenChargeUserPoint() {
        // given
        long userId = 1L;
        long amount = 0L;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> pointService.chargeUserPoint(userId, amount));
    }

    @Test
    @DisplayName("포인트 충전 요청 및 히스토리 기록")
    void givenUserPoint_whenChargeUserPoint() {
        // given
        long userId = 1L;
        long lastPoint = 1000L;
        long chargePoint = 2000L;
        long finalPoint = lastPoint + chargePoint;
        long chargeTime = System.currentTimeMillis();
        Mockito.when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, lastPoint, System.currentTimeMillis()));
        Mockito.when(userPointTable.insertOrUpdate(userId, finalPoint)).thenReturn(new UserPoint(userId, finalPoint, chargeTime));
        Mockito.when(pointHistoryTable.insert(userId, chargePoint, TransactionType.CHARGE, chargeTime)).thenReturn(new PointHistory(1L, userId, chargePoint, TransactionType.CHARGE, chargeTime));

        //when
        CommonResponse userPointResponse = pointService.chargeUserPoint(userId, chargePoint);
        UserPoint userPoint = (UserPoint) userPointResponse.getData();

        //then
        assertEquals(finalPoint, userPoint.point());

        Mockito.verify(userPointTable, Mockito.times(1)).selectById(userId);
        Mockito.verify(userPointTable, Mockito.times(1)).insertOrUpdate(userId, finalPoint);

        Mockito.verify(pointHistoryTable, Mockito.times(1)).insert(eq(userId), eq(chargePoint), eq(TransactionType.CHARGE), anyLong());
    }
}
