<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${mapperNamespace}">

    <!-- AUTO-GENERATED-START: base-columns -->
    <sql id="Base_Column_List">
        ${baseColumns}
    </sql>
    <!-- AUTO-GENERATED-END: base-columns -->

    <!-- AUTO-GENERATED-START: page-query -->
    <select id="${pageQueryId}" resultType="${resultType}">
        SELECT
        <include refid="Base_Column_List"/>
        FROM ${tableName}
        WHERE 1 = 1
        ${logicDeleteSql!""}    </select>
    <!-- AUTO-GENERATED-END: page-query -->

    <!-- CUSTOM-START -->
    <!-- 开发自己写的 SQL 放这里 -->
    <!-- CUSTOM-END -->

</mapper>
