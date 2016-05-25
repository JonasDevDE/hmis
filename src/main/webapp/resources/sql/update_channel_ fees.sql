-- select f.`ID`,f.`FEETYPE`,f.`NAME`,f.`FEE`,f.`FFEE` from fee f join item i on f.`ITEM_ID`=i.`ID` WHERE i.`DTYPE`='ServiceSession' 
-- and i.`RETIRED`=false and f.`RETIRED`=false and
-- f.`FEETYPE`='OwnInstitution' and f.`NAME`='Hospital Fee';


-- update fee f join item i on f.`ITEM_ID`=i.`ID` SET f.`FEE`=17.39 WHERE i.`DTYPE`='ServiceSession' and i.`RETIRED`=false and f.`RETIRED`=false and
-- f.`FEETYPE`='OwnInstitution' and f.`NAME`='On-Call Fee';
-- 
-- update fee f join item i on f.`ITEM_ID`=i.`ID` SET f.`FFEE`=17.39 WHERE i.`DTYPE`='ServiceSession' and i.`RETIRED`=false and f.`RETIRED`=false and
-- f.`FEETYPE`='OwnInstitution' and f.`NAME`='On-Call Fee';

select f.`ID`,f.`FEETYPE`,f.`NAME`,f.`FEE`,f.`FFEE` from fee f join item i on f.`ITEM_ID`=i.`ID` WHERE i.`DTYPE`='ServiceSession' and i.`RETIRED`=false and f.`RETIRED`=false and
f.`FEETYPE`='OwnInstitution' and f.`NAME`='On-Call Fee';