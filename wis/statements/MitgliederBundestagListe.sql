select k."name", p."name", b."name"
from mdbs m left join partei p on m.parteiid = p.parteiid
            join kandidat k on m.kandidatid = k.kandidatid
            left join bundesland b on b.bundeslandid = m.bundeslandid