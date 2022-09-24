package com.bancatlan.atmauthorizer.exception;


import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PrivilegeError implements IError {
    /**
     * ESB SUFFIX MINTS ENTERPRISE SERVICE BUS
     * SERVICE SUFFIX TO BANK SERVICES
     * */
    USER_WITHOUT_PERMISSION_FOR_THE_ACCOUNT(203,"¡Su usuario no tiene los permisos en la cuenta origen!"),
    IT_IS_SCHEDULED_SAVINGS_ACCOUNTS(203,"¡La cuenta origen es de Ahorro Programado y no es posible validar la fecha en la cual estarán disponibles los fondos para su uso!"),;


    private Integer code;
    private String message;

    /**
     * Constructor generico. Recibe el codigo y la descripcion del enum
     * @param code codigo
     * @param message mensaje
     */
    PrivilegeError(final Integer code, final String message){
        this.code = code;
        this.message = message;
    }

    /**
     * Obtiene el codigo del enum
     * @return el codigo
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Define el codigo del enum
     * @param code el nuevo codigo
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * Obtiene el mensaje del enum
     * @return el mensaje
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Define el mensaje del enum
     * @param message el nuevo mensaje
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Retorna el codigo y descripcion del enum para impresion en logs
     * @return el string con la informacion
     */
    @Override
    public String toString(){
        return getCode() + "|" + getMessage();
    }
}

