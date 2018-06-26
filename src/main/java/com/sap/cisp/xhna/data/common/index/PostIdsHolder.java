package com.sap.cisp.xhna.data.common.index;

import java.io.Serializable;
import java.util.List;


public  class PostIdsHolder implements Serializable {
    private static final long serialVersionUID = -4111321131672595215L;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getIdList() {
        return idList;
    }

    public void setIdList(List<String> idList) {
        this.idList = idList;
    }

    private String path;
    private List<String> idList;

    public PostIdsHolder() {
    }

    public PostIdsHolder(String path, List<String> idList) {
        this.path = path;
        this.idList = idList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PostIdsHolder that = (PostIdsHolder) o;

        if (idList != null ? !idList.equals(that.idList)
                : that.idList != null)
            return false;
        if (path != null ? !path.equals(that.path) : that.path != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (idList != null ? idList.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[PostIdsContainer: ");
        sb.append("path=").append(path);
        sb.append(", idList=").append(idList);
        sb.append("]");
        return sb.toString();
    }
}
