package com.ericsson.component.aia.services.exteps.event.router.util;

public class SamplePojo {

    private Long enbS1ApId;
    private Long mmeS1ApId;
    private Long macroEndbId;

    public SamplePojo(final Long endbs1Id, final Long mmes1Id, final Long endbId) {
        enbS1ApId = endbs1Id;
        mmeS1ApId = mmes1Id;
        macroEndbId = endbId;
    }

    public Long getENB_UE_S1AP_ID() {
        return enbS1ApId;
    }

    public void setENB_UE_S1AP_ID(final Long eNB_UE_S1AP_ID) {
        enbS1ApId = eNB_UE_S1AP_ID;
    }

    public Long getMME_UE_S1AP_ID() {
        return mmeS1ApId;
    }

    public void setMME_UE_S1AP_ID(final Long mME_UE_S1AP_ID) {
        mmeS1ApId = mME_UE_S1AP_ID;
    }

    public Long getENODEB_ID_MACRO_ENODEB_ID() {
        return macroEndbId;
    }

    public void setENODEB_ID_MACRO_ENODEB_ID(final Long eNODEB_ID_MACRO_ENODEB_ID) {
        macroEndbId = eNODEB_ID_MACRO_ENODEB_ID;
    }

}
