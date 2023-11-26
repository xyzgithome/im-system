package com.lld.im.common;

import com.lld.im.common.exception.ApplicationExceptionEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;


@Data
@Accessors(chain = true)
public class ResponseVO<T> {

    private int code;

    private String msg;

    private T data;

    public static <T> ResponseVO<T> successResponse(T data) {
        ResponseVO<T> success = new ResponseVO<T>().setCode(200).setMsg("success");

        return Objects.isNull(data) ? success : success.setData(data);
    }

    public static <T> ResponseVO<T> success() {
        return successResponse(null);
    }

    public static<T> ResponseVO<T> fail(int code, String msg) {
        return new ResponseVO<T>().setCode(code).setMsg(msg);
    }

    public static<T> ResponseVO<T> fail(ApplicationExceptionEnum enums) {
        return fail(enums.getCode(), enums.getError());
    }

    public static ResponseVO fail() {
        return fail(500, "系统内部异常");
    }

    public boolean isOk(){
        return this.code == 200;
    }
}
