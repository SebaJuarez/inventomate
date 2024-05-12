import { useAuth0 } from "@auth0/auth0-react";
import { useUser } from "../hook/useUser";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { SideNavbar } from "../components/SideNavbar";
import { getCompany } from "../utils/Database.service";
import { CompanyBanner } from "../components/CompanyBanner";
import { Searcher } from "../components/Searcher";
import { CompanyBranches } from "../components/CompanyBranches";

export const Company = () => {

  const { currentUser, setUser } = useUser()

  const { isAuthenticated, getAccessTokenSilently } = useAuth0();

  const [company, setCompany] = useState<Company | null>(null)

  useEffect(() => {

    const getToken = async () => {
      const accessToken = await getAccessTokenSilently()
      setUser(accessToken)

      const userCompany = await getCompany(accessToken)
      setCompany(userCompany)
    }

    isAuthenticated && getToken()

  }, [isAuthenticated])

  return (
    <main className="-text--color-black flex">
      <section className="hidden relative lg:block w-64">
        <SideNavbar />
      </section>
      <section className="m-auto mt-4 w-11/12 lg:w-7/12 xl:w-7/12">
        <CompanyBanner />
        <section className="my-4">
          <h2 className="font-bold -text--color-semidark-violet py-2 text-lg">Branches</h2>
          <div className="grid grid-cols-2">
            {
              currentUser?.roles.some(rol => rol.idRol === 1)
              &&
              <Link to='/company/register-branch' className="-bg--color-border-very-lightest-grey p-2 rounded-lg font-semibold -text--color-mate-dark-violet w-32 text-center">+ Add Branch</Link>
            }
            <Searcher />
          </div>
          <CompanyBranches company={company} />
        </section>
      </section>
    </main>
  );
};
